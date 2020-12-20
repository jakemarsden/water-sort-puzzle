package com.github.jakemarsden.watersortpuzzle;

import java.util.stream.*;

import static java.lang.String.*;
import static java.util.Objects.*;

final class Fluid {

    public static Fluid of(Type type, int amount) {
        requireNonNull(type, "type");
        if (amount <= 0) {
            throw new IllegalArgumentException("amount: " + amount);
        }
        return new Fluid(type, amount);
    }

    private final Type type;
    private final int amount;

    private Fluid(Type type, int amount) {
        this.type = type;
        this.amount = amount;
    }

    public Type getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }

    public Fluid withAdditionalAmount(int amount) {
        if (amount + getAmount() < 0) {
            throw new IllegalArgumentException("amount: " + amount);
        }
        return new Fluid(getType(), getAmount() + amount);
    }

    public Stream<Type> streamLayers() {
        return Stream.generate(this::getType)//
                .limit(getAmount());
    }

    public enum Type {

        LightGreen('0'),
        Grey('1'),
        LightBlue('2'),
        Orange('3'),
        Pink('4'),
        Yellow('5'),
        Red('6'),
        DarkGreen('7'),
        Purple('8'),
        DarkBlue('9'),
        Brown('A'),
        SwampGreen('B');

        public static Type fromChar(char ch) {
            return Stream.of(values())
                    .filter(value -> value.ch == ch)
                    .findAny()
                    .orElseThrow(() -> new IllegalArgumentException(//
                            format("No such fluid type: '%s'", ch)));
        }

        private final char ch;

        Type(char ch) {
            this.ch = ch;
        }

        public char toChar() {
            return ch;
        }
    }
}
