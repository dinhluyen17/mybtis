package com.example.batis2.service;

import com.example.batis2.entity.Person;
import com.example.batis2.repository.CarRepo;
import com.example.batis2.repository.PersonRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@AllArgsConstructor
public class JpaService {
    private PersonRepo personRepo;
    private CarRepo carRepo;

    public List<Person> getAllPerson() {
        List<Person> all = personRepo.findAll();
        return all;
    }

    public Person findById(Long id) {
        return personRepo.findById(id).orElse(null);
    }

    public Person addPerson(Person person) {
        return personRepo.save(person);
    }

    public Person updatePerson(Person person, long id) {
        Person found = personRepo.findById(id).orElse(null);
        if(found != null) {
            found.setName(person.getName());
            found.setAge(person.getAge());
            found.setCars(person.getCars());
        }
        Person save = personRepo.save(found);
        return save;
    }

    public void delete(Long id) {
        personRepo.deleteById(id);
    }
}
