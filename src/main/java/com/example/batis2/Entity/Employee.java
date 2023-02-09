package com.example.batis2.Entity;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Employee {
    private long id;
    private String firstName;
    private String lastName;
    private String emailAddress;
}

