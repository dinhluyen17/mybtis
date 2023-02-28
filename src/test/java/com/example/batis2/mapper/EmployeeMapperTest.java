package com.example.batis2.mapper;

import com.example.batis2.entity.Employee;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;


class EmployeeMapperTest {
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
    void addEmployee() {
        SqlSession sqlSession = null;
        try {
            sqlSession = sqlSessionFactory.openSession();
            EmployeeMapper employeeMapper = sqlSession.getMapper(EmployeeMapper.class);
            Employee employee = Employee.builder()
                    .id(1)
                    .firstName("henry")
                    .lastName("dao")
                    .emailAddress("henrydao!!#")
                    .build();
            int a = employeeMapper.addEmployee(employee);
            System.out.println(a + "add successfully!");
            sqlSession.commit();
        } catch (Exception e) {
            System.out.println(e);
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
    }

    @Test
    void updateEmployee() {
        SqlSession sqlSession = null;
        try {
            sqlSession = sqlSessionFactory.openSession();
            EmployeeMapper employeeMapper = sqlSession.getMapper(EmployeeMapper.class);
            Employee employee = Employee.builder()
                    .id(1)
                    .firstName("Luyen")
                    .lastName("Dao Dinh")
                    .emailAddress("dinhluyen45@gmail.com")
                    .build();
            int a = employeeMapper.updateEmployee(employee);
            System.out.println(a + "update successfully!");
            sqlSession.commit();
        } catch (Exception e) {
            System.out.println(e);
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
    }

    @Test
    void deleteEmployee() {
    }
}