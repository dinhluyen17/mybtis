package com.example.batis2;

import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class Service {
    private final RestTemplate restTemplate;
    private final String requestPythonUrl = "http://localhost:8000/json-to-qasm2";

    public Service(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getQasmCode(String jsonString) {
        StringBuilder stringBuilder = new StringBuilder();
        String prefix = "OPENQASM 2.0;\n" +
                "include \"qelib1.inc\";\n";
        List<List<String>> jsonList = getJsonList(jsonString);
        List<String> jsonRequest = getJsonRequest(jsonList);
        System.out.println("------");
        jsonRequest.forEach(System.out::println);
        for (String request : jsonRequest) {
            String encodedRequestJsonCols = encodeStringRequest(request);
            System.out.println("request encoded " + encodedRequestJsonCols);
            String result = restTemplate.postForObject(requestPythonUrl, encodedRequestJsonCols, String.class);
            analysizeResult(result, jsonList.get(0));
        }
        return null;
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
    public static List<String> splitJsonColumnWithParameterizedGate(String input) {
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
        System.out.println("what is json string :" + list);
        String prefix = "{\"cols\":[";
        String endfix = "]}";
        List<String> requestList = new ArrayList<>();
        for (List<String> strings : list) {
            StringBuilder url = new StringBuilder("[");
            System.out.println("string ban dau dau: " + strings);
            for (int j = 0; j < strings.size(); j++) {
                System.out.println("log string get J: " + j + strings.get(j));
                if(strings.get(j).startsWith("{id")) {
                    System.out.println("da chay vao day tai j = " + j);
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
                finalRs.append(rsList.get(i));
                finalRs.append("\n");
            }
            return finalRs;
        }
        for(String x : finalRsWithNoIndex) {
//            System.out.println("check x" + x);
            String gateName = x.replaceAll("\\s.*", "");
            System.out.println(gateName);
            finalRsNoIndex.add(gateName);
        }
        int idx = 0; //count index of gate in response list
        int swapFlag = -1; //flag for the first time swap gate appear in the json column
        int swapIdxCount = 0;
        if(!jsonList.contains("Swap")) {
            for(int i = 0; i < jsonList.size(); i++) {
                if(!jsonList.get(i).equals("1")) {
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
        System.out.println(finalRs);
        return finalRs;
    }
}
