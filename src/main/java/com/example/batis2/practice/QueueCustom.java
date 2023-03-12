package com.example.batis2.practice;

import java.util.LinkedList;
import java.util.List;

public class QueueCustom<T> {
    LinkedList<T> list;

    public List<T> add(T t) {
        if(list == null) {
            list = new LinkedList<>();
        }
        list.addFirst(t);
        return list;
    }

    public T pop() {
        if(list.size() > 0) {
            T val = list.getLast();
            list.removeLast();
            return val;
        }
        return null;
    }

    public T peek() {
        if(list.size() > 0) {
            T val = list.getLast();
            return val;
        }
        return null;
    }

    public static void main(String[] args) {
        QueueCustom<Integer> queueCustom = new QueueCustom<>();
        queueCustom.add(1);
        queueCustom.add(2);
        queueCustom.add(3);
        queueCustom.add(5);
        System.out.println(queueCustom.list);
        System.out.println(queueCustom.peek());
        System.out.println(queueCustom.pop());
        System.out.println(queueCustom.pop());
        System.out.println(queueCustom.pop());
        System.out.println(queueCustom.list);
    }
}
