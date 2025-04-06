package com.limachi.arss.utils;

import com.mojang.datafixers.util.Pair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BiMap<L, R> implements Iterable<Pair<L, R>> {
    private final HashMap<L, R> leftToRight = new HashMap<>();
    private final HashMap<R, L> rightToLeft = new HashMap<>();

    @Override
    public Iterator<Pair<L, R>> iterator() {
        return pairSet().iterator();
    }

    public int size() {
        return leftToRight.size();
    }

    public boolean isEmpty() {
        return leftToRight.isEmpty();
    }

    public boolean containsLeft(L obj) {
        return leftToRight.containsKey(obj);
    }

    public boolean containsRight(R obj) {
        return rightToLeft.containsKey(obj);
    }

    public boolean has(Pair<L, R> pair) {
        R r = leftToRight.get(pair.getFirst());
        return r != null && r.equals(pair.getSecond());
    }

    public R getRight(L left) {
        return leftToRight.get(left);
    }

    public L getLeft(R right) {
        return rightToLeft.get(right);
    }

    public Pair<L, R> put(L left, R right) {
        if (left == null || right == null)
            throw new IllegalArgumentException("Neither left nor right can be null.");
        R r = leftToRight.put(left, right);
        L l = rightToLeft.put(right, left);
        return new Pair<>(l, r);
    }

    public Pair<L, R> put(Pair<L, R> pair) {
        R r = leftToRight.put(pair.getFirst(), pair.getSecond());
        L l = rightToLeft.put(pair.getSecond(), pair.getFirst());
        return new Pair<>(l, r);
    }

    public Pair<L, R> removeLeft(L left) {
        R r = leftToRight.remove(left);
        L l = rightToLeft.remove(r);
        return new Pair<>(l, r);
    }

    public Pair<L, R> removeRight(R right) {
        L l = rightToLeft.remove(right);
        R r = leftToRight.remove(l);
        return new Pair<>(l, r);
    }

    public Pair<L, R> remove(Pair<L, R> pair) {
        R r = leftToRight.remove(pair.getFirst());
        L l = rightToLeft.remove(pair.getSecond());
        return new Pair<>(l, r);
    }

    public void putAll(Map<? extends L, ? extends R> m) {
        m.forEach(this::put);
    }

    public void putAll(Iterable<L> lefts, Iterable<R> rights) {
        var itLeft = lefts.iterator();
        var itRight = rights.iterator();
        while (itLeft.hasNext() && itRight.hasNext())
            put(itLeft.next(), itRight.next());
        if (itLeft.hasNext() || itRight.hasNext())
            throw new IllegalArgumentException("The left and right iterables must have the same length.");
    }

    public void putAll(Iterable<Pair<L, R>> pairs) {
        pairs.forEach(this::put);
    }

    public void clear() {
        leftToRight.clear();
        rightToLeft.clear();
    }

    public Set<L> leftSet() {
        return leftToRight.keySet();
    }

    public Set<R> rightSet() {
        return rightToLeft.keySet();
    }

    public Stream<Pair<L, R>> stream() {
        return leftToRight.entrySet().stream().map(e -> new Pair<>(e.getKey(), e.getValue()));
    }

    public Set<Pair<L, R>> pairSet() {
        return stream().collect(Collectors.toSet());
    }
}
