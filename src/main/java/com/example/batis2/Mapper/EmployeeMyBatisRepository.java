package com.example.batis2.Mapper;

import com.example.batis2.Entity.Employee;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface EmployeeMyBatisRepository {
    @Select("select * from employees")
    List<Employee> findAll();

    @Select("SELECT * FROM employees WHERE id = #{id}")
    Employee findById(long id);

    @Delete("DELETE FROM employees WHERE id = #{id}")
    int deleteById(long id);

    @Insert("INSERT INTO employees(id, first_name, last_name,email_address) " +
            " VALUES (#{id}, #{firstName}, #{lastName}, #{emailId})")
    int insert(Employee employee);

    @Update("Update employees set first_name=#{firstName}, " +
            " last_name=#{lastName}, email_address=#{emailId} where id=#{id}")
    int update(Employee employee);
}

