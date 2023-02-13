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
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/v1")
@AllArgsConstructor
public class EmpController {
    private final EmployeeMyBatisRepository employeeMyBatisRepository;
    private final EmpMapper empMapper;
    private  final Service service;

    @GetMapping(path = "{id}")
    public Employee findbyid(@PathVariable Integer id) {
        return null;
    }

    @GetMapping(path = "")
    public List<Employee> getAll() {
        return employeeMyBatisRepository.findAll();
    }

    @PostMapping(path = "/convertJsonToQasm")
    public ResponseEntity<?> convert(@RequestBody String json) {
        String demo = service.getQasmCode(json);
        System.out.println(demo);
        return ResponseEntity.ok(demo);
    }

//    public static List<List<Objects>> getJsonList(String json) {
//        String json2 = "{\"cols\":[[1,{\"id\":\"Z^ft\",\"arg\":\"sin(pi)\"}]]}";
//        String json3 = json2.replace("\"", "");
//        String json4 = json3.substring(7, json3.length() - 2);
//
//        List<String> list = List.of(json4.split("],|]"));
//        List<String> list2 = list.stream().map(item -> item.replace("[", "")).collect(Collectors.toList());
//        List<List<String>> demo = new ArrayList<>();
//        for(String str : list2) {
//            if(!str.contains("arg")) {
//                List<String> col = List.of(str.split(","));
//                demo.add(col);
//            } else {
//                demo.add(Collections.singletonList(str));
//            }
//        }
//        System.out.println("demo request: " + demo.get(0));
//        getJsonRequest(demo);
//        return null;
//    }
//
//    public static void getJsonRequest(List<List<String>> list) {
//        String prefix = "{\"cols\":[";
//        String endfix = "]";
//
//        for (List<String> strings : list) {
//            StringBuilder url = new StringBuilder("[");
//            for (int j = 0; j < strings.size(); j++) {
//                if(!strings.get(j).equals("1")) {
//                    url.append("\"");
//                    url.append(strings.get(j));
//                    url.append("\"");
//                }
//                else {
//                    url.append(strings.get(j));
//                }
//                if(j < strings.size() - 1) {
//                    url.append(", ");
//                } else {
//                    url.append("]");
//                }
//            }
//            System.out.println(prefix + url + endfix);
//        }
//
//    }

}
