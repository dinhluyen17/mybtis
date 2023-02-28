package com.example.batis2.mapper;

import com.example.batis2.entity.Employee;

import java.util.List;

public interface EmployeeMapper {
    List<Employee> findAll();
    Employee findById(Integer id);

    int addEmployee(Employee employee);

    int updateEmployee(Employee employee);

    int deleteEmployee(Integer id);
}
