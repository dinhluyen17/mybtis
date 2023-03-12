package com.example.batis2.practice.designPatterns.creational.factory;

public class BMW implements Car{
    @Override
    public Car getCar() {
        System.out.println("This is bmw");
        return new BMW();
    }
}
