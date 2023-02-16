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
        StringBuilder demo = service.getQasmCode(json);
        System.out.println(demo);
        return ResponseEntity.ok(demo);
    }

    @PostMapping(path = "/converQasmToJson")
    public ResponseEntity<?> convertToJson(@RequestBody String qasm) {
        StringBuilder jsonString = service.getJsonCode(qasm);
        System.out.println(jsonString);
        return ResponseEntity.ok(jsonString);
    }



}
