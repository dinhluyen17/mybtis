package com.example.batis2.controller;

import com.example.batis2.entity.Person;
import com.example.batis2.service.JpaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/v2")
@RestControllerAdvice
public class PersonController {
    @Autowired private JpaService jpaService;

    @GetMapping(path = "")
    public ResponseEntity getAllPerson() {
        return ResponseEntity.ok(jpaService.getAllPerson());
    }

    @PostMapping (path = "")
    public ResponseEntity addPerson(@RequestBody Person person) {
//        Person person = new Person();
        jpaService.addPerson(person);
        return ResponseEntity.ok(person);
    }

    @PutMapping(path = "{id}")
    public ResponseEntity updatePerson(@PathVariable Long id, @RequestBody Person person) {
//        Person person = new Person();
        jpaService.updatePerson(person, id);
        return ResponseEntity.ok(person);
    }

    @DeleteMapping(path = "{id}")
    public ResponseEntity delete(@PathVariable Long id) {
        jpaService.delete(id);
        return ResponseEntity.ok("success!");
    }
}
