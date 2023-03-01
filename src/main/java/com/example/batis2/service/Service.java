package com.example.batis2.service;

import com.example.batis2.exception.ExternalApiException;
import org.springframework.web.client.RestTemplate;

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

    //v2: starting --------------------------------------------------------------------------------------------------------------
    public boolean checkSupportedGate(List<List<String>> json) {
        List<String> supportedGates = Arrays.asList("\"Z\"", "\"Swap\"", "\"Y\"", "\"X\"", "\"H\"", "\"Z^½\"", "\"X^½\"", "\"Y^½\"", "\"Z^-½\"", "\"X^-½\"", "\"Y^-½\"", "\"Z^¼\"", "\"X^¼\"", "\"Y^¼\"","\"Z^-¼\"", "\"X^-¼\"", "\"Y^-¼\"", "\"Measure\"", "1", "\"•\"", "\"…\"");
        List<String> supptedGatesWithParameter = Arrays.asList("{\"id\":\"Z^ft\"", "{\"id\":\"Rzft\"", "{\"id\":\"Y^ft\"", "{\"id\":\"Ryft\"", "{\"id\":\"X^ft\"", "{\"id\":\"Rxft\"");
        List<String> controlGateColumes = Arrays.asList("\"•\"","\"Z\"","\"Y\"","\"X\"","\"H\"");
        for(List<String> col : json) {
            if(!col.contains("\"•\"")) {
                for(String gate : col) {
                    if(!supportedGates.contains(gate.trim())) {
                        if(!gate.startsWith("{")) {
                            return false;
                        }
                        gate = gate.trim().split(",")[0];
                        boolean flag = false;
                        for(String paramGate : supptedGatesWithParameter) {
                            if(paramGate.indexOf(gate.trim()) != -1) {
                                flag = true;
                            }
                        }
                        if(!flag) {
                            return false;
                        }
                    }
                }
            }
            else {
                for(String gate : col) {
                    if(!controlGateColumes.contains(gate) && !gate.equals("1")) {
                        throw new ExternalApiException("control fdsakfjslfjds error");
                    }
                }
            }
        }
        return true;
    }
    public StringBuilder getQasmCode(String json) {
        StringBuilder returnCode = new StringBuilder();
        String listString = json.substring(9, json.length() - 2);
        List<String> listString2 = splitJsonColsToList(listString);
        List<List<String>> finalList = new ArrayList<>();
        for(String colString : listString2) {
            String beforeSplitString = colString.replaceAll("\\[", "").replaceAll("]", "");
            List<String> strings = splitJsonColumnWithParameterizedGate(beforeSplitString);
            finalList.add(strings);
        }

        boolean b = checkSupportedGate(finalList);

        String encodedRequest = encodeStringRequest(json);
        String result = restTemplate.postForObject(requestPythonUrl + "json-to-qasm2", encodedRequest, String.class);
        List<String> qasmCode = Arrays.asList(result.split("\n\n"));
        StringBuilder qasmOnly = new StringBuilder();
        for(int i = 2; i< qasmCode.size(); i++) {
            qasmOnly.append(qasmCode.get(i).replaceAll("\n", ""));
        }
        List<String> qasmAsList = Arrays.asList(String.valueOf(qasmOnly).split(";"));
        int mark = 0;
        List<List<String>> rs = new ArrayList<>();
        for(int i = 0; i < finalList.size(); i++) {
            int actualColSize = finalList.get(i).stream()
                    .filter(gate -> !gate.equals("1"))
                    .collect(Collectors.toList())
                    .size();

            List<String> qasmCol = new ArrayList<>();
            if(finalList.get(i).contains("\"•\"") || finalList.get(i).contains("\"Swap\"")) {
                for(int j = mark; j < qasmAsList.size(); j++) {
                    qasmCol.add(qasmAsList.get(j));
                    mark++;
                    if(qasmCol.size() == actualColSize - 1) {
                        break;
                    }
                }
            }
            else {
                for(int j = mark; j < qasmAsList.size(); j++) {
                    qasmCol.add(qasmAsList.get(j));
                    mark++;
                    if(qasmCol.size() == actualColSize) {
                        break;
                    }
                }
            }
            rs.add(qasmCol);
        }

        StringBuilder tata = new StringBuilder();
        for(int i = 0; i < rs.size(); i++) {
            StringBuilder col = new StringBuilder();
            for (int j = 0; j < rs.get(i).size(); j++) {
                col.append(rs.get(i).get(j))
                        .append(";")
                        .append("\n");
            }
            StringBuilder stringBuilder = analysizeResult(String.valueOf(col), finalList.get(i));
            tata.append(stringBuilder);
            tata.append("//\n");
        }
        returnCode.append(qasmCode.get(0))
                .append("\n\n")
                .append(qasmCode.get(1))
                .append("\n\n")
                .append(tata);
        return returnCode;
    }

    public List<String> splitJsonColsToList(String input) {
        List<String> column = new ArrayList<>();
        int startPosition = 0;
        boolean isInQuotes = false;
        for (int currentPosition = 0; currentPosition < input.length(); currentPosition++) {
            if (input.charAt(currentPosition) == '[' || input.charAt(currentPosition) == ']') {
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

    public StringBuilder analysizeResult(String rsColumn, List<String> jsonList) {
        StringBuilder finalRs = new StringBuilder(); //save the return value
        List<String> finalRsWithNoIndex = List.of(rsColumn.replaceAll("\\n", "").split(";"));
        List<String> finalRsNoIndex = new ArrayList<>();
        //no need to handle result if rs column contain control gate (python backend has done it)
        if(jsonList.contains("\"•\"")) {
            StringBuilder controlRs = new StringBuilder();
            List<Integer> indexOfGates = new ArrayList<>(Arrays.asList(0)); //control gate always get first position
            for(int i = 0; i < jsonList.size(); i++) {
                if(!jsonList.get(i).equals("\"•\"") && !jsonList.get(i).equals("1")) {
                    indexOfGates.add(i);
                } else if (jsonList.get(i).equals("\"•\"")) {
                    indexOfGates.set(0, i);
                }
            }
            System.out.println("Gate index wiht control " + indexOfGates);
            List<String> lineOfCodeSubtractIndex = new ArrayList<>();
            for(String line : finalRsWithNoIndex) {
                String gate = line.split("\\s")[0];
                lineOfCodeSubtractIndex.add(gate);
            }
            System.out.println("Gate index wiht control " + lineOfCodeSubtractIndex);
            for(int i = 0; i < lineOfCodeSubtractIndex.size(); i++) {
                String val = String.format("%s q[%d],q[%d];\n", lineOfCodeSubtractIndex.get(i), indexOfGates.get(0), indexOfGates.get(i + 1));
                controlRs.append(val);
            }
            System.out.println("fdfdfd" + controlRs);
            return new StringBuilder(controlRs);
        }
        for(String x : finalRsWithNoIndex) {
            String gateName = x.replaceAll("\\s.*", "");
            finalRsNoIndex.add(gateName);
        }
        int idx = 0; //count index of gate in response list
        int swapFlag = -1; //flag for the first time swap gate appear in the json column
        int swapIdxCount = 0;
        if(!jsonList.contains("\"Swap\"")) {
            for(int i = 0; i < jsonList.size(); i++) {
                if(jsonList.get(i).equals("\"Measure\"")) {
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
                if (jsonList.get(i).equals("\"Swap\"")) {
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


    //ending



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

    public String encodeStringRequest(String stringRequest) {
       return Base64.getEncoder().encodeToString(stringRequest.getBytes());
    }


    //--------------

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
//        JsonObject jsonObject = new JsonObject();
//        JsonArray jsonArray = new JsonArray();
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
                List<String> gateIdx;
                try {
                    gateIdx = gates.stream()
                            .filter(gate -> gate.length() > 0)
                            .map(gate -> gate.contains("measure") ? gate.substring(8, 12) : gate.substring(gate.length() - 4))
                            .collect(Collectors.toList());
                } catch (StringIndexOutOfBoundsException ex){
                    throw new ExternalApiException("Syntax error");
                }

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
