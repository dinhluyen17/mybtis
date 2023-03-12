package com.example.batis2.practice.designPatterns.creational.factory;

public class CarFactory {
    public static Car getCar(String brand) {
        if(brand.equals("Audi")) {
            return new Audi();
        } else {
            return new BMW();
        }
    }

    public static void main(String[] args) {
        Car audi = CarFactory.getCar("Audi");
        audi.getCar();

    }
}
