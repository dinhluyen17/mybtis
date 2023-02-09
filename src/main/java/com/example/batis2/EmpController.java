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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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

}
