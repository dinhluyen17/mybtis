package com.example.batis2;

import com.example.batis2.Entity.Employee;
import com.example.batis2.Entity.MyBatisUtil;
import com.example.batis2.Mapper.EmpMapper;
import com.example.batis2.Mapper.EmployeeMyBatisRepository;
import lombok.AllArgsConstructor;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/v1")
@AllArgsConstructor
public class EmpController {
    private final EmployeeMyBatisRepository employeeMyBatisRepository;
    private final EmpMapper empMapper;

    @GetMapping(path = "{id}")
    public Employee findbyid(@PathVariable Integer id) {
//        SqlSession sqlSession = .getSqlSessionFactory().openSession();
//        try{
//            EmpMapper userMapper = sqlSession.getMapper(EmpMapper.class);
//            return userMapper.findById(id);
//        }finally{
//            sqlSession.close();
//        }
        return null;
    }

    @GetMapping(path = "")
    public List<Employee> getAll() {
//        SqlSession sqlSession = MyBatisUtil.getSqlSessionFactory().openSession();
//        try{
//            EmpMapper userMapper = sqlSession.getMapper(EmpMapper.class);
//            return userMapper.findAll();
//        }finally{
//            sqlSession.close();
//        }
        return employeeMyBatisRepository.findAll();
    }

    @PostMapping(path = "/convertJsonToQasm")
    public ResponseEntity<?> convert(@RequestBody String json) {
        JSONObject json = new JSONObject(string);
        System.out.println(json.toString());
        String technology = json.getString("technology");
        System.out.println(technology);

        System.out.println(json);
        RestTemplate restTemplate = new RestTemplate();
        String requestUrl = "http://localhost:8000/json-to-qasm";
        String demo = restTemplate.postForObject(requestUrl, json, String.class);
        System.out.println(demo);
        return ResponseEntity.ok(demo);
    }

    ArrayList<String> jsonStringToArray(String jsonString) throws JSONException {
        ArrayList<String> stringArray = new ArrayList<String>();
        JSONob jsonArray = new JSONArray(jsonString);
        for (int i = 0; i < jsonArray.length(); i++) {
            stringArray.add(jsonArray.getString(i));
        }

        return stringArray;
    }

    public List<List<Objects>> getJsonList(String json) {
        String json2 = "{\"cols\":[[1,1,\"Z^½\"],[\"Z^¼\",\"Z^¼\"],[1,\"Swap\",\"Swap\"],[1,1,\"Measure\"]]}";
        System.out.println(json2);
        return null;
    }

    public static List<List<Objects>> getJsonList(String json) {
        String json2 = "{\"cols\":[[1,{\"id\":\"Z^ft\",\"arg\":\"sin(pi)\"}]]}";
        String json3 = json2.replace("\"", "");
        String json4 = json3.substring(7, json3.length() - 2);

        List<String> list = List.of(json4.split("],|]"));
        List<String> list2 = list.stream().map(item -> item.replace("[", "")).collect(Collectors.toList());
        List<List<String>> demo = new ArrayList<>();
        for(String str : list2) {
            if(!str.contains("arg")) {
                List<String> col = List.of(str.split(","));
                demo.add(col);
            } else {
                demo.add(Collections.singletonList(str));
            }
        }
        System.out.println("demo request: " + demo.get(0));
        getJsonRequest(demo);
        return null;
    }

    public static void getJsonRequest(List<List<String>> list) {
        String prefix = "{\"cols\":[";
        String endfix = "]";

        for (List<String> strings : list) {
            StringBuilder url = new StringBuilder("[");
            for (int j = 0; j < strings.size(); j++) {
                if(!strings.get(j).equals("1")) {
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
            System.out.println(prefix + url + endfix);
        }

    }

}
