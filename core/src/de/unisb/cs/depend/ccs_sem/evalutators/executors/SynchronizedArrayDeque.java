package de.unisb.cs.depend.ccs_sem.evalutators.executors;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;


public class SynchronizedArrayDeque<E> extends ArrayDeque<E> {

    private static final long serialVersionUID = 6000304351755373800L;

    @Override
    public synchronized boolean add(E e) {
        return super.add(e);
    }

    @Override
    public synchronized void addFirst(E e) {
        super.addFirst(e);
    }

    @Override
    public synchronized void addLast(E e) {
        super.addLast(e);
    }

    @Override
    public synchronized void clear() {
        super.clear();
    }

    @Override
    public synchronized ArrayDeque<E> clone() {
        return super.clone();
    }

    @Override
    public synchronized boolean contains(Object o) {
        return super.contains(o);
    }

    @Override
    public synchronized Iterator<E> descendingIterator() {
        return super.descendingIterator();
    }

    @Override
    public synchronized E element() {
        return super.element();
    }

    @Override
    public synchronized E getFirst() {
        return super.getFirst();
    }

    @Override
    public synchronized E getLast() {
        return super.getLast();
    }

    @Override
    public synchronized boolean isEmpty() {
        return super.isEmpty();
    }

    @Override
    public synchronized Iterator<E> iterator() {
        return super.iterator();
    }

    @Override
    public synchronized boolean offer(E e) {
        return super.offer(e);
    }

    @Override
    public synchronized boolean offerFirst(E e) {
        return super.offerFirst(e);
    }

    @Override
    public synchronized boolean offerLast(E e) {
        return super.offerLast(e);
    }

    @Override
    public synchronized E peek() {
        return super.peek();
    }

    @Override
    public synchronized E peekFirst() {
        return super.peekFirst();
    }

    @Override
    public synchronized E peekLast() {
        return super.peekLast();
    }

    @Override
    public synchronized E poll() {
        return super.poll();
    }

    @Override
    public synchronized E pollFirst() {
        return super.pollFirst();
    }

    @Override
    public synchronized E pollLast() {
        return super.pollLast();
    }

    @Override
    public synchronized E pop() {
        return super.pop();
    }

    @Override
    public synchronized void push(E e) {
        super.push(e);
    }

    @Override
    public synchronized E remove() {
        return super.remove();
    }

    @Override
    public synchronized boolean remove(Object o) {
        return super.remove(o);
    }

    @Override
    public synchronized E removeFirst() {
        return super.removeFirst();
    }

    @Override
    public synchronized boolean removeFirstOccurrence(Object o) {
        return super.removeFirstOccurrence(o);
    }

    @Override
    public synchronized E removeLast() {
        return super.removeLast();
    }

    @Override
    public synchronized boolean removeLastOccurrence(Object o) {
        return super.removeLastOccurrence(o);
    }

    @Override
    public synchronized int size() {
        return super.size();
    }

    @Override
    public synchronized Object[] toArray() {
        return super.toArray();
    }

    @Override
    public synchronized <T> T[] toArray(T[] a) {
        return super.toArray(a);
    }

    @Override
    public synchronized boolean addAll(Collection<? extends E> c) {
        return super.addAll(c);
    }

    @Override
    public synchronized boolean containsAll(Collection<?> c) {
        return super.containsAll(c);
    }

    @Override
    public synchronized boolean removeAll(Collection<?> c) {
        return super.removeAll(c);
    }

    @Override
    public synchronized boolean retainAll(Collection<?> c) {
        return super.retainAll(c);
    }

    @Override
    public synchronized String toString() {
        return super.toString();
    }

    @Override
    public synchronized boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public synchronized int hashCode() {
        return super.hashCode();
    }

}
