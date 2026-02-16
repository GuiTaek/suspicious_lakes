package com.gmail.guitaekm.enderlakes.util;


import java.util.*;
import java.util.function.Function;

public class IntervalFunction<B> implements Function<Double, B> {
    private final List<Double> intervals;
    private final List<Function<Double, B>> definitions;
    private final String name;
    private final double startValue;

    public IntervalFunction(double startValue, Map<Double, Function<Double, B>> intervalDefinitions, String name) {
        this.startValue = startValue;
        this.name = name;
        this.intervals = new ArrayList<>();
        this.definitions = new ArrayList<>();
        this.intervals.addAll(intervalDefinitions.keySet());
        this.intervals.sort(Double::compare);
        for (double elem : this.intervals) {
            this.definitions.add(intervalDefinitions.get(elem));
        }
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public B apply(Double x) {
        if (x < this.startValue) {
            throw new IllegalArgumentException("The interval function %s got a value before its definition".formatted(this.toString()));
        }
        if (this.intervals.getLast() < x) {
            throw new IllegalArgumentException("The intervalFunction %s got a value after its definition".formatted(this.toString()));
        }
        // idea from ChatGPT
        int binarySearchResult = Collections.binarySearch(this.intervals, x);
        // weird code, why does java do that
        if (binarySearchResult < 0) {
            binarySearchResult = -binarySearchResult - 1;
        }
        return this.definitions.get(binarySearchResult).apply(x);
    }
}
