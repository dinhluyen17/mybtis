package com.example.batis2.service;

import com.example.batis2.entity.Person;
import com.example.batis2.repository.CarRepo;
import com.example.batis2.repository.PersonRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@RunWith(SpringRunner.class)

class JpaServiceTest {
//    @Autowired
    @Autowired
    private PersonRepo personRepo;

    @Autowired
    private CarRepo carRepo;

    @Autowired
    private JpaService jpaService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void getAllPerson() {
        List<Person> all = jpaService.getAllPerson();
        all.forEach(System.out::println);

    }

    @Test
    void findById() {
    }

    @Test
    void addPerson() {
    }

    @Test
    void updatePerson() {
    }

    @Test
    void delete() {
    }
}