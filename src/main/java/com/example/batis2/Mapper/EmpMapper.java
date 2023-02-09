package com.example.batis2.Mapper;

import com.example.batis2.Entity.Employee;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface EmpMapper {
    List<Employee> findAll();

    Employee findById(long id);

    int deleteById(long id);

    int insert(Employee employee);

    int update(Employee employee);
}
