package nl.han.ica.datastructures;

import java.util.NoSuchElementException;

public class HANLinkedList<T> implements IHANLinkedList<T> {

    private Node<T> head;
    private int size;

    private static class Node<T> {
        T value;
        Node<T> next;

        Node(T value) {
            this.value = value;
            this.next = null;
        }
    }

    @Override
    public void addFirst(T value) {
        Node<T> newNode = new Node<>(value);
        newNode.next = head;
        head = newNode;
        size++;
    }

    @Override
    public void removeFirst() {
        if (head == null) {
            throw new NoSuchElementException("List is empty");
        }
        head = head.next;
        size--;
    }

    @Override
    public T getFirst() {
        if (head == null) {
            throw new NoSuchElementException("List is empty");
        }
        return head.value;
    }

    @Override
    public void clear() {
        head = null;
        size = 0;
    }

    @Override
    public void insert(int index, T value) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        if (index == 0) {
            addFirst(value);
            return;
        }

        Node<T> current = head;
        for (int i = 0; i < index - 1; i++) {
            current = current.next;
        }

        Node<T> newNode = new Node<>(value);
        newNode.next = current.next;
        current.next = newNode;
        size++;
    }

    @Override
    public void delete(int pos) {
        if (pos < 0 || pos >= size) {
            throw new IndexOutOfBoundsException("Position: " + pos + ", Size: " + size);
        }

        if (pos == 0) {
            removeFirst();
            return;
        }

        Node<T> current = head;
        for (int i = 0; i < pos - 1; i++) {
            current = current.next;
        }
        current.next = current.next.next;
        size--;
    }

    @Override
    public T get(int pos) {
        if (pos < 0 || pos >= size) {
            throw new IndexOutOfBoundsException("Position: " + pos + ", Size: " + size);
        }

        Node<T> current = head;
        for (int i = 0; i < pos; i++) {
            current = current.next;
        }
        return current.value;
    }

    @Override
    public int getSize() {
        return size;
    }
}
