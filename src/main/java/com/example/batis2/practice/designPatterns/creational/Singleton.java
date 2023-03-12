package com.example.batis2.practice.designPatterns.creational;

import java.security.Signature;

public class Singleton {
    private static Singleton instance;

    private int a;

    private Singleton() {
    }

    public static Singleton getInstance() {
        if(instance == null) {
            instance = new Singleton();
        }
        return instance;
    }

    public static void setInstance(Singleton instance) {
        Singleton.instance = instance;
    }

    public static void main(String[] args) {
        Singleton singleton1 = Singleton.getInstance();
        singleton1.a = 10;
        Singleton singleton2 = Singleton.getInstance();
        System.out.println(singleton2);
        System.out.println(singleton1);
        System.out.println(singleton1.a);
        System.out.println(singleton2.a);
    }
}
