package com.example.batis2;

import com.example.batis2.Exception.ExternalApiException;
import org.springframework.web.client.RestTemplate;

import javax.print.DocFlavor;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class Service {
    private final RestTemplate restTemplate;
    private final String requestPythonUrl = "http://localhost:8000/";
    private int countMeasureGate; //set global variable for number of time a measure gate is appeared in the circuit
    public Service(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public StringBuilder getQasmCode(String jsonString) {
        String prefix = "OPENQASM 2.0;\n" +
                "include \"qelib1.inc\";\n\n\n";
        countMeasureGate = 0; //set default measuregate appear time to 0;

        StringBuilder concatResult = new StringBuilder(prefix); //variable for final rs
        List<List<String>> jsonList = getJsonList(jsonString);
        concatResult.append(getQregAndCreg(jsonList));
        List<String> jsonRequest = getJsonRequest(jsonList);

        for (int i = 0; i < jsonRequest.size(); i++) {
            String encodedRequestJsonCols = encodeStringRequest(jsonRequest.get(i));
            String result = restTemplate.postForObject(requestPythonUrl + "json-to-qasm2", encodedRequestJsonCols, String.class);
            StringBuilder rs = analysizeResult(result, jsonList.get(i));
            concatResult.append(rs);
            concatResult.append("//\n");
        }
        return concatResult;
    }

    public List<List<String>> getJsonList(String json) {
        String json3 = json.replace("\"", "");
        String json4 = json3.substring(7, json3.length() - 2);

        List<String> list = List.of(json4.split("],|]"));
        List<String> list2 = list.stream().map(item -> item.replace("[", "")).collect(Collectors.toList());
        List<List<String>> demo = new ArrayList<>();
        for(String str : list2) {
            if(!str.contains("arg")) {
                List<String> col = List.of(str.split(","));
                demo.add(col);
            } else {
                List<String> colWithArgGates = splitJsonColumnWithParameterizedGate(str);
                demo.add(colWithArgGates);
            }
        }
        return demo;
    }

    //split json string with special gate to array list
    public List<String> splitJsonColumnWithParameterizedGate(String input) {
        List<String> column = new ArrayList<>();
        int startPosition = 0;
        boolean isInQuotes = false;
        for (int currentPosition = 0; currentPosition < input.length(); currentPosition++) {
            if (input.charAt(currentPosition) == '{' || input.charAt(currentPosition) == '}') {
                isInQuotes = !isInQuotes;
            }
            else if (input.charAt(currentPosition) == ',' && !isInQuotes) {
                column.add(input.substring(startPosition, currentPosition));
                startPosition = currentPosition + 1;
            }
        }

        String lastCol = input.substring(startPosition);
        if (lastCol.equals(",")) {
            column.add("");
        } else {
            column.add(lastCol);
        }
        return column;
    }

    public List<String> getJsonRequest(List<List<String>> list) {
        String prefix = "{\"cols\":[";
        String endfix = "]}";
        List<String> requestList = new ArrayList<>();
        for (List<String> strings : list) {
            StringBuilder url = new StringBuilder("[");
            for (int j = 0; j < strings.size(); j++) {
                if(strings.get(j).startsWith("{id")) {
                    for(int k = 0; k < strings.get(j).length() - 1; k++) {
                        url.append(strings.get(j).charAt(k));
                        if(strings.get(j).charAt(k) == '{'
                                || strings.get(j).charAt(k + 1) == ':'
                                || strings.get(j).charAt(k) == ':'
                                || strings.get(j).charAt(k + 1) == 'a'
                                || strings.get(j).charAt(k) == 'g'
                                || strings.get(j).charAt(k + 1) == ','
                                || strings.get(j).charAt(k + 1) == '}') {
                            url.append("\"");
                        }
                    }
                    url.append("}");
                }
                else if(!strings.get(j).equals("1")) {
                    url.append("\"");
                    url.append(strings.get(j));
                    url.append("\"");
                }
                else {
                    url.append(strings.get(j));
                }

                if(j < strings.size() - 1) {
                    url.append(", ");
                } else {
                    url.append("]");
                }
            }
            requestList.add(prefix + url + endfix);
        }
        return requestList;
    }

    public String encodeStringRequest(String stringRequest) {
       return Base64.getEncoder().encodeToString(stringRequest.getBytes());
    }

    public StringBuilder analysizeResult(String rsColumn, List<String> jsonList) {
        StringBuilder finalRs = new StringBuilder(); //save the return value

        List<String> rsList = List.of(rsColumn.split("\\n\\n"));
        List<String> finalRsWithNoIndex = List.of(rsList.get(2).replaceAll("\\n", "").split(";"));
        List<String> finalRsNoIndex = new ArrayList<>();
        //no need to handle result if rs column contain control gate (python backend has done it)
        if(jsonList.contains("•")) {
            for(int i = 2; i < rsList.size(); i++) {
                finalRs.append(rsList.get(i).trim());
                if(i < rsList.size() - 1) {
                    finalRs.append("\n");
                }
            }
            return finalRs;
        }
        for(String x : finalRsWithNoIndex) {
            String gateName = x.replaceAll("\\s.*", "");
            finalRsNoIndex.add(gateName);
        }
        int idx = 0; //count index of gate in response list
        int swapFlag = -1; //flag for the first time swap gate appear in the json column
        int swapIdxCount = 0;
        if(!jsonList.contains("Swap")) {
            for(int i = 0; i < jsonList.size(); i++) {
                if(jsonList.get(i).equals("Measure")) {
                    finalRsNoIndex.set(idx, finalRsNoIndex.get(idx) + " q[" + i + "]" + " -> m" + countMeasureGate + "[0];");
                    idx++;
                    countMeasureGate++;
                }
                else if(!jsonList.get(i).equals("1")) {
                    finalRsNoIndex.set(idx, finalRsNoIndex.get(idx) + " q[" + i + "];");
                    idx++;
                }
            }
        } else {
            for(int i = 0; i < jsonList.size(); i++) {
                if (jsonList.get(i).equals("Swap")) {
                    if(swapFlag == -1) {
                        swapFlag = idx;
                    }
                    swapIdxCount++;
                    if(swapIdxCount < 2) {
                        finalRsNoIndex.set(swapFlag, finalRsNoIndex.get(swapFlag) + " q[" + i + "],");
                        idx++; //idx only increase one time only
                    } else {
                        finalRsNoIndex.set(swapFlag, finalRsNoIndex.get(swapFlag) + "q[" + i + "];");
                    }
                } else if(!jsonList.get(i).equals("1")) {
                    finalRsNoIndex.set(idx, finalRsNoIndex.get(idx) + " q[" + i + "];");
                    idx++;
                }
            }
        }

        //save rs to final string builder
        for(String str : finalRsNoIndex) {
            finalRs.append(str);
            finalRs.append("\n");
        }
        return finalRs;
    }

    //part 2 of final result (get number of qubits and number of measurement gates)
    public StringBuilder getQregAndCreg(List<List<String>> jsonList) {
        StringBuilder finalRsPart2 = new StringBuilder();
        StringBuilder measureGate = new StringBuilder();
        int maxQubits = 2; //config minium number of qubits
        int countOfMeasureGate = 0;
        for(int i = 0; i < jsonList.size(); i++) {
            if(maxQubits < jsonList.get(i).size()) {
                maxQubits = jsonList.get(i).size();
            }
            for(int j = 0; j < jsonList.get(i).size(); j++) {
                if(jsonList.get(i).get(j).equals("Measure")) {
                    measureGate.append(String.format("creg m%s[1];  // Measurement: row=%s,col=%s\n", countOfMeasureGate, j, i));
                    countOfMeasureGate++;
                }
            }
        }
        measureGate.append("\n\n");
        finalRsPart2.append(String.format("qreg q[%s];\n", maxQubits));
        return finalRsPart2.append(measureGate);
    }

    public StringBuilder getJsonCode(String qasm) {
        StringBuilder jsonFinal = new StringBuilder("{\"cols\":[");
        List<String> detachCode = List.of(qasm.split("\n\n"));
        StringBuilder qasmPart = new StringBuilder();
        for(int i = 2; i < detachCode.size(); i++) {
            qasmPart.append(detachCode.get(i));
        }
        List<String> listQasm = splitQasmCodeByColumn(String.valueOf(qasmPart));
        List<String> listQasm2 = new ArrayList<>(listQasm);
        List<StringBuilder> listOfQasmCodeRequest = getListOfQasmCodeRequest(detachCode, listQasm, listQasm2);
        for(int i = 0; i < listOfQasmCodeRequest.size(); i++) {
            String encodeQasmRequest = encodeStringRequest(String.valueOf(listOfQasmCodeRequest.get(i)));
            String jsonCol = restTemplate.postForObject(requestPythonUrl + "qasm-to-json2", encodeQasmRequest, String.class);
            if(jsonCol.equals("{\"cols\":[]}")) {
                continue;
            }
            else if(jsonCol.contains("Swap")) { //backend python return 2 array columne with colume got swap gate inside
                StringBuilder jsonColsWithPositionSwapCol = getJsonColsWithPositionSwapCol(jsonCol.substring(9, jsonCol.length() - 2), listQasm2.get(i));
                jsonFinal.append(jsonColsWithPositionSwapCol);
            }
            else if(jsonCol.contains("•")) {
                StringBuilder jsonColsWithPositionControlCol = getJsonColsWithPositionControlCol(jsonCol.substring(9, jsonCol.length() - 2), listQasm2.get(i));
                jsonFinal.append(jsonColsWithPositionControlCol);
            }
            else {
                StringBuilder jsonColsWithPosition = getJsonColsWithPosition(jsonCol.substring(10, jsonCol.length() - 3), listQasm2.get(i));
                jsonFinal.append(jsonColsWithPosition);
            }

            if(i < listOfQasmCodeRequest.size() - 1) {
                jsonFinal.append(",");
            }
        }
        if(jsonFinal.charAt(jsonFinal.length() - 1) == ',') {
            jsonFinal.deleteCharAt(jsonFinal.length() -1);
        }
        jsonFinal.append("]}");
        System.out.println("fianl json la gi: " + jsonFinal);
        return jsonFinal;
    }

    public List<String> splitQasmCodeByColumn(String rawQasmString) {
        return List.of(rawQasmString.split("//"));
    }

    public List<StringBuilder> getListOfQasmCodeRequest(List<String> headPart, List<String> qasmPart, List<String> listQasm2) {
        List<StringBuilder> requestList = new ArrayList<>();
        for(int i = 0; i < qasmPart.size(); i++) {
            //if request true the return to request list
            boolean check = handleRequestWithNoDivideCols(headPart, requestList, qasmPart.get(i), i, listQasm2); //check if a qasm code input by customer is valid or not
            if(check){
                StringBuilder request = new StringBuilder(headPart.get(0));
                request.append(headPart.get(1));
                request.append(qasmPart.get(i));
                requestList.add(request);
            }
        }
        return requestList;
    }

    public StringBuilder getJsonColsWithPosition(String json, String requestQasm) {
        if(json.length() == 0) {
            return new StringBuilder("");
        }
        if(json.contains("•") || json.contains("Swap")) {
            return new StringBuilder(json);
        }
        StringBuilder rs = new StringBuilder("[");
        List<Integer> takenPosition = new ArrayList<>();
        for(int i = 1; i < requestQasm.length() - 1; i++) {
            if(requestQasm.charAt(i) == '[' && requestQasm.charAt(i - 1) == 'q') {
                takenPosition.add(Character.getNumericValue(requestQasm.charAt(i + 1)));
            }
        }

        List<String> colArr = splitJsonColumnWithParameterizedGate(json);
        if(colArr.size() == takenPosition.get(takenPosition.size() - 1) + 1)  {
            rs.append(json);
        } else {
            int gateNum = 0;
            for(int i = 0; i <= takenPosition.get(takenPosition.size() - 1); i++) {
                if(!takenPosition.contains(i)) {
                    rs.append(1);
                    rs.append(",");
                } else {
                    rs.append(colArr.get(gateNum));
                    gateNum++;
                    if(gateNum < takenPosition.size()) {
                        rs.append(",");
                    }
                }
            }
        }
        if(rs.length() > 0) {
            rs.append("]");
        }
        return rs;
    }

    public StringBuilder getJsonColsWithPositionSwapCol(String  jsonSwapCol, String qasmRequest) {
        //split all json cols and remove swap gate, and keep normal gate only!
        List<String> gateListExcludeSwap = List.of(jsonSwapCol.replaceAll("\\[", "").replaceAll("]", "").split(","))
                .stream().filter(gate -> !gate.equals("\"Swap\"") && !gate.equals("1")).collect(Collectors.toList());
        List<Integer> takenPosition = new ArrayList<>(); //2 first position is for swap gate
        takenPosition.add(0);
        takenPosition.add(0);
        int countSwap = 0; //swap can only appear 2 times

        StringBuilder swapGateIndex = new StringBuilder();
        for(int i = 0; i < qasmRequest.length(); i++) {
            swapGateIndex.append(qasmRequest.charAt(i));
            if(i == 0) continue;
            if(qasmRequest.charAt(i) == '[' && qasmRequest.charAt(i - 1) == 'q') {
                int idxValue = Character.getNumericValue(qasmRequest.charAt(i + 1));
                if(!String.valueOf(swapGateIndex).contains("swap") || countSwap == 2) {
                    takenPosition.add(idxValue);
                } else {
                    takenPosition.set(countSwap, idxValue);
                    countSwap++;
                }
            }
        }

        int jsonLen = Collections.max(takenPosition);
        List<String> rs = new ArrayList<>();

        int gateIdx = 0; //mark idx for this list
        for(int i = 0; i <= jsonLen; i++) {
            if(!takenPosition.contains(i)) {
                rs.add("1");
            }
            else {
                if(takenPosition.indexOf(i) == 0 || takenPosition.indexOf(i) == 1) {
                    rs.add("\"Swap\"");
                }
                else {
                    rs.add(gateListExcludeSwap.get(gateIdx));
                    gateIdx++;
                }
            }
        }
        return new StringBuilder(rs.toString());
    }

    public StringBuilder getJsonColsWithPositionControlCol(String  jsonControl, String qasmRequest) {
        List<String> listOfGatesExcludeControl = List.of(jsonControl.replaceAll("\\[", "").replaceAll("]", "").split(","))
                .stream().filter(gate -> !gate.equals("\"•\"") && !gate.equals("1")).collect(Collectors.toList());

        //System.out.println("list gate " + listOfGatesExcludeControl);

        List<Integer> takenPosition = new ArrayList<>(); //1 first position is for control gate
        takenPosition.add(-1);
        boolean isControl = true; //swap can only appear 2 times

        StringBuilder controlCol = new StringBuilder();
        for(int i = 2; i < qasmRequest.length(); i++) {
            controlCol.append(qasmRequest.charAt(i));
            if(qasmRequest.charAt(i) == '[' && qasmRequest.charAt(i - 1) == 'q' && qasmRequest.charAt(i - 2) == ' ') {
                int idxValue = Character.getNumericValue(qasmRequest.charAt(i + 1));
                if(!isControl) {
                    takenPosition.add(idxValue);
                } else {
                    if(takenPosition.get(0) == -1) {
                        takenPosition.set(0, idxValue);
                    }
                }
                isControl = !isControl;
            }
        }

        //System.out.println("taken position " + takenPosition);

        int jsonLen = Collections.max(takenPosition);
        int idx = 0;
        List<String> rs = new ArrayList<>();
        for(int i = 0; i <= jsonLen; i++) {
            if(!takenPosition.contains(i)) {
                rs.add("1");
            }
            else {
                if(takenPosition.indexOf(i) == 0) {
                    rs.add("\"•\"");
                }
                else {
                    rs.add(listOfGatesExcludeControl.get(idx));
                    idx++;
                }
            }
        }
        return new StringBuilder(rs.toString());
    }

    public boolean handleRequestWithNoDivideCols(List<String> headPart, List<StringBuilder> requestList, String qasm, int idxQasmPart, List<String> listQasm2) {
        Pattern pattern = Pattern.compile("c[a-z]\\sq\\[\\d]");
        Matcher matcher = pattern.matcher(qasm);
        if(matcher.find()) {
            System.out.println("check control column >>>>: " + qasm);
            List<String> colsWithControlGate = List.of(qasm.trim().split(";"));
            Optional<String> check = colsWithControlGate.stream().map(gate -> gate.trim()).filter(gate -> !gate.startsWith("c")).findFirst();
            if(check.isPresent()) {
                throw new ExternalApiException("column with control gate can only contain control gate");
            } else {
                List<String> listControlGateIdx = getQubitIndexNumber(qasm);
                if(listControlGateIdx.size() < 2) {
                    throw new ExternalApiException("Syntax error in control gate column");
                }
                String firstValueIdx = listControlGateIdx.get(0);
                List<String> listGates = new ArrayList<>(Arrays.asList(listControlGateIdx.get(1)));
                for(int i = 2; i < listControlGateIdx.size(); i++) {
                    if(i % 2 == 0) {
                        if(!listControlGateIdx.get(i).equals(firstValueIdx)) {
                            throw new ExternalApiException("Control gate in one column need to be in the same position!");
                        }
                    } else {
                        if(listGates.contains(listControlGateIdx.get(i))) {
                            throw new ExternalApiException("Same gate position in the control column");
                        } else {
                            listGates.add(listControlGateIdx.get(i));
                        }
                    }
                }
            }
            return true;
        }
        else {
            if(!qasm.contains("swap")) {
                List<List<String>> code = new ArrayList<>();
                List<List<String>> checkDupIdx = new ArrayList<>();

                List<String> gates = Arrays.asList(qasm.replaceAll("\n", "").split(";"));
                if(gates.size() <= 1) { //if request contains only one gate or less then no need to check for duplicate position of gates
                    return true;
                }
                List<String> gateIdx = gates.stream()
                        .filter(gate -> gate.length() > 0)
                        .map(gate -> gate.contains("measure") ? gate.substring(8, 12) : gate.substring(gate.length() - 4))
                        .collect(Collectors.toList());
                checkDupIdx.add(new ArrayList<>(Arrays.asList(gateIdx.get(0))));
                code.add(new ArrayList<>(Arrays.asList(gates.get(0))));
                for(int i = 1; i < gates.size(); i++) {
                    boolean addedGateIdx = false;
                    for(int j = 0; j < checkDupIdx.size(); j++) {
                        if(!checkDupIdx.get(j).contains(gateIdx.get(i))) {
                            checkDupIdx.get(j).add(gateIdx.get(i));
                            code.get(j).add(gates.get(i));
                            addedGateIdx = true;
                            break;
                        }
                    }
                    if(!addedGateIdx) {
                        checkDupIdx.add(new ArrayList<>(Arrays.asList(gateIdx.get(i))));
                        code.add(new ArrayList<>(Arrays.asList(gates.get(i))));
                    }
                }
                if(code.size() == 1) return true; //there is no duplication in code
                listQasm2.remove(idxQasmPart);
                for(int i = 0; i < code.size(); i++) {
                    StringBuilder refactorRequest = new StringBuilder(headPart.get(0))
                            .append("\n\n\n")
                            .append(headPart.get(1))
                            .append("\n\n\n");
                    StringBuilder newQasm = new StringBuilder();
                    for(int j = 0; j < code.get(i).size(); j++) {
                        newQasm.append(code.get(i).get(j));
                        newQasm.append(";\n");
                    }
                    refactorRequest.append(newQasm);
                    requestList.add(refactorRequest);
                    listQasm2.add(i + idxQasmPart, String.valueOf(newQasm));
                }
                return false;
            }
            else {
                List<String> gateIdx = getQubitIndexNumber(qasm);
                Set<String> checkDuplicate = new HashSet<>(gateIdx);
                if(gateIdx.size() != checkDuplicate.size()) {
                    throw new ExternalApiException("Same position in the swap column gate");
                }
            }
        }
        return true;
    }


    //return a list q[line-number] in the string
    public List<String> getQubitIndexNumber(String qasm) {
        Pattern pattern2 = Pattern.compile("q\\[\\d\\]");
        Matcher matcher2 = pattern2.matcher(qasm);
        List<String> list = new ArrayList<>();
        while(matcher2.find()) {
            list.add(matcher2.group());
        }
        return list;
    }
}
