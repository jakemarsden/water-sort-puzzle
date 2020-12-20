package com.github.jakemarsden.watersortpuzzle;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static java.util.Objects.*;

final class Puzzle {

    public static Puzzle withInitialState(Collection<? extends Tube> tubes) {
        var tubesCopy = tubes.toArray(Tube[]::new);
        return new Puzzle(tubesCopy);
    }

    private final Tube[] tubes;

    private Puzzle(Tube[] tubes) {
        this.tubes = tubes;
    }

    public boolean isSolved() {
        // FIXME: don't insist on always being full: if there is no more than one partially-full
        //  tube of each type, it's done
        return Stream.of(tubes).allMatch(//
                tube -> tube.isEmpty() || (tube.isFull()) && tube.containsSingleFluidType());
    }

    public Tube getNthTube(int n) {
        return tubes[n];
    }

    public int getTubeCount() {
        return tubes.length;
    }

    public Stream<Tube> streamTubes() {
        return Arrays.stream(tubes);
    }

    public Puzzle withUpdatedTube(int tubeIdx, UnaryOperator<Tube> tubeOperator) {
        requireNonNull(tubeOperator);
        var currTube = getNthTube(tubeIdx);
        var nextTube = tubeOperator.apply(currTube);
        if (nextTube == currTube) {
            // no-op, avoid the copy
            return this;
        }
        var tubes = Arrays.copyOf(this.tubes, this.tubes.length);
        tubes[tubeIdx] = nextTube;
        return new Puzzle(tubes);
    }
}
