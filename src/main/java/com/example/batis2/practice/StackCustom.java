package com.example.batis2.practice;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.ArrayList;
import java.util.List;

public class StackCustom<T> {
    private List<T> list;
    public List<T> add(T t) {
        if(list == null) {
            list = new ArrayList<>();
        }
        list.add(t);
        return list;
    }

    public T pop() {
        if(list.size() > 0) {
            T value = list.get(list.size() - 1);
            list.remove(list.size() - 1);
            return value;
        }
        return null;
    }

    public T peek() {
        if(list.size() > 0) {
            T value = list.get(list.size() - 1);
            return value;
        }
        return null;
    }

    public static void main(String[] args) {
        StackCustom<Integer> stack = new StackCustom<>();
        stack.add(1);
        stack.add(2);
        stack.add(5);
        stack.add(4);
        System.out.println(stack.list);
        System.out.println(stack.peek());
        System.out.println(stack.peek());
        System.out.println(stack.pop());
        System.out.println(stack.pop());
        System.out.println(stack.list);

    }

}
