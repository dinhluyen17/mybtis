package com.example.batis2;

import com.example.batis2.entity.Employee;
import com.example.batis2.mapper.EmployeeMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.io.Reader;
import java.util.List;


class Service2Test {
    private static SqlSessionFactory sqlSessionFactory;

    @BeforeEach
    void setUp() {
        try {
            Reader reader = Resources.getResourceAsReader("mybatis-config.xml");
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
            System.out.println("fd");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void getAll() {
        SqlSession sqlSession;
        sqlSession = sqlSessionFactory.openSession();
        try {
            sqlSession = sqlSessionFactory.openSession();
//
            EmployeeMapper mapper = sqlSession.getMapper(EmployeeMapper.class);


            List<Employee> all = mapper.findAll();
            all.forEach(System.out::println);
            System.out.println("----");
            Employee employee = mapper.findById(10011);
            System.out.println(employee);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (sqlSession != null) {
                sqlSession.close();
            }
        }

    }
}