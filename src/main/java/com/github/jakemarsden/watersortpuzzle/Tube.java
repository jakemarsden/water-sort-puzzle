package com.github.jakemarsden.watersortpuzzle;

import java.util.*;
import java.util.stream.*;

import static java.lang.String.*;
import static java.util.Objects.*;

final class Tube {

    /**
     * Top of the tube is represented by the first char, and the bottom by the last. Any unused
     * capacity at the top must be represented by whitespace chars.
     */
    public static Tube fromString(String str) {
        int charIdx = 0;
        // skip leading whitespace
        for (; charIdx < str.length(); charIdx++) {
            char ch = str.charAt(charIdx);
            if (!Character.isWhitespace(ch)) {
                break;
            }
        }

        // in the worst case, all fluids will have amount=1 so alloc enough space for that upfront
        Deque<Fluid> contents = new ArrayDeque<>(str.length() - charIdx);

        // populate contents, accumulating into amount until the next fluid type is hit, at which
        // point it insert into contents and continue with the next type of fluid
        Fluid.Type accumulatingType = null;
        int accumulatingAmount = 0;

        for (; charIdx < str.length(); charIdx++) {
            char ch = str.charAt(charIdx);
            if (Character.isWhitespace(ch)) {
                throw new IllegalArgumentException(//
                        format("Levitating fluid at index %d: \"%s\"", charIdx - 1, str));
            }
            Fluid.Type nextType;
            try {
                nextType = Fluid.Type.fromChar(ch);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(//
                        format("Invalid fluid type at index %d: \"%s\"", charIdx, str));
            }

            if (nextType != accumulatingType) {
                // this fluid is finished; want to move onto the next one, so append it to contents
                if (accumulatingType != null) {
                    contents.addLast(Fluid.of(accumulatingType, accumulatingAmount));
                }
                accumulatingType = nextType;
                accumulatingAmount = 0;
            }
            accumulatingAmount++;
        }
        if (accumulatingType != null) {
            contents.addLast(Fluid.of(accumulatingType, accumulatingAmount));
        }
        return new Tube(str.length(), contents);
    }

    private final int capacity;
    private final Deque<Fluid> contents;

    private Tube(int capacity, Deque<Fluid> contents) {
        this.capacity = capacity;
        this.contents = contents;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getUsedCapacity() {
        return streamFluids()//
                .mapToInt(Fluid::getAmount)//
                .sum();
    }

    public boolean isFull() {
        return getSpareCapacity() == 0;
    }

    public int getSpareCapacity() {
        return getCapacity() - getUsedCapacity();
    }

    public boolean isEmpty() {
        return contents.isEmpty();
    }

    public Optional<Fluid> getTopFluid() {
        var fluidOrNull = contents.peekFirst();
        return Optional.ofNullable(fluidOrNull);
    }

    public Optional<Fluid.Type> getFluidTypeAtDepth(int depth) {
        if (isEmpty()) {
            return Optional.empty();
        }
        var spareCap = getSpareCapacity();
        if (spareCap > depth) {
            return Optional.empty();
        }
        return streamLayers()//
                .skip(depth - spareCap)//
                .findFirst();
    }

    public boolean containsSingleFluidType() {
        return contents.size() == 1;
    }

    public Tube withAdditionalFluid(Fluid fluid) {
        requireNonNull(fluid);
        if (fluid.getAmount() == 0) {
            return this;
        }
        if (fluid.getAmount() > getSpareCapacity()) {
            throw new IllegalStateException(//
                    format("Insufficient capacity for %s: \"%s\"", fluid, this));
        }

        var currFirst = contents.peekFirst();
        Deque<Fluid> newContents;
        if (currFirst != null && currFirst.getType() == fluid.getType()) {
            var merged = currFirst.withAdditionalAmount(fluid.getAmount());
            newContents = new ArrayDeque<>(contents);
            newContents.removeFirst();
            newContents.addFirst(merged);
        } else {
            newContents = new ArrayDeque<>(contents.size() + 1);
            newContents.addAll(contents);
            newContents.addFirst(fluid);
        }
        return new Tube(getCapacity(), newContents);
    }

    public Tube withoutTopFluid() {
        if (isEmpty()) {
            return this;
        }
        Deque<Fluid> newContents = new ArrayDeque<>(contents);
        newContents.removeFirst();
        return new Tube(getCapacity(), newContents);
    }

    public Stream<Fluid> streamFluids() {
        return contents.stream();
    }

    public Stream<Fluid.Type> streamLayers() {
        return streamFluids()//
                .flatMap(Fluid::streamLayers);
    }

    /**
     * Top of the tube is represented by the first char, and the bottom by the last. Any unused
     * capacity at the top is represented by space chars.
     */
    @Override
    public String toString() {
        var len = getCapacity();
        var buf = new StringBuilder(len);
        streamLayers()//
                .mapToInt(Fluid.Type::toChar)//
                .forEach(buf::appendCodePoint);
        // compute padding first so buf only needs to shift (at most) once
        buf.insert(0, " ".repeat(len - buf.length()));
        return buf.toString();
    }
}
