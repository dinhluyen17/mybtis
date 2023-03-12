package com.example.batis2.practice;

public class LinkedListDemo<T> {
    private Node head;
    private Node rear;

    public T addHead(T data) {
        Node<T> newNode = new Node<>(data);
        if(head == null) {
            head = newNode;
        } else {
            head.setNext(newNode);
            newNode.setPrevious(head);
        }

        return data;
    }
}
