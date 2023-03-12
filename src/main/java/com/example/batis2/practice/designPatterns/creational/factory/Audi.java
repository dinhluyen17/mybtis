package com.example.batis2.practice.designPatterns.creational.factory;

public class Audi implements Car {

    @Override
    public Car getCar() {
        System.out.println("This is audi");
        return this;
    }
}
