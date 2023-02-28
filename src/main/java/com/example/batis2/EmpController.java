package com.example.batis2;

import com.example.batis2.entity.Employee;
import com.example.batis2.mapper.EmpMapper;
import com.example.batis2.out.EmployeeMyBatisRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping(path = "/v1")
public class EmpController {
    private final EmployeeMyBatisRepository employeeMyBatisRepository;
    private final EmpMapper empMapper;
    private  final Service service;

    @Value("${python-backend-quantum.json-to-qasm}")
    private String baeldungPresentation;

    @Autowired
    public EmpController(EmployeeMyBatisRepository employeeMyBatisRepository, EmpMapper empMapper, Service service) {
        this.employeeMyBatisRepository = employeeMyBatisRepository;
        this.empMapper = empMapper;
        this.service = service;
    }

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
        System.out.println(baeldungPresentation);
        return ResponseEntity.ok(demo);
    }

//    @PostMapping(path = "/convertJsonToQasm2")
//    public ResponseEntity<?> convert2(@RequestBody String json) {
//        StringBuilder demo = service.getJsonList2(json);
//        System.out.println(baeldungPresentation);
//        return ResponseEntity.ok(demo);
//    }


    @PostMapping(path = "/converQasmToJson")
    public ResponseEntity<?> convertToJson(@RequestBody String qasm) {
        StringBuilder jsonString = service.getJsonCode(qasm);
        System.out.println(jsonString);
        return ResponseEntity.ok(jsonString);
    }




}
