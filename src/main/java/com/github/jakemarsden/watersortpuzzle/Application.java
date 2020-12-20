package com.github.jakemarsden.watersortpuzzle;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static java.lang.String.*;

public class Application {

    public static final int EXIT_CODE_SUCCESS = 0;
    public static final int EXIT_CODE_NO_SOLUTION = 1;
    public static final int EXIT_CODE_BAD_INPUT = 2;

    private static final boolean DEBUG = true;

    /**
     * Solve level #141 by default.
     */
    private static final String[] DEFAULT_INITIAL_STATE = {
            "0123",
            "3345",
            "5367",
            "8892",
            "57A9",
            "694B",
            "2A9A",
            "B412",
            "4B68",
            "50B6",
            "11A0",
            "0877",
            "    ",
            "    "
    };

    /**
     * @param args the puzzle to solve, represented by an array of {@code Strings} (one {@code
     *         String} per tube), or {@code null} or an empty array to solve an arbitrary puzzle
     */
    public static void main(String[] args) {
        var serializedPuzzle = (args != null && args.length != 0) ? args : DEFAULT_INITIAL_STATE;

        Puzzle puzzle;
        try {
            puzzle = parsePuzzle(serializedPuzzle);
        } catch (IllegalArgumentException e) {

            System.out.println("!!! MALFORMED PUZZLE !!!");
            System.out.println(e.getLocalizedMessage());
            if (DEBUG) {
                e.printStackTrace(System.err);
            }
            System.exit(EXIT_CODE_BAD_INPUT);
            return;
        }

        try {
            validatePuzzle(puzzle);
        } catch (IllegalArgumentException e) {

            writePrettyPuzzle(puzzle, System.out::print);
            System.out.println("!!! INVALID PUZZLE !!!");
            System.out.println(e.getLocalizedMessage());
            if (DEBUG) {
                e.printStackTrace(System.err);
            }
            System.exit(EXIT_CODE_BAD_INPUT);
            return;
        }

        var solver = PuzzleSolver.forPuzzle(puzzle);
        var solution = solver.solve();

        if (solution.isPresent()) {
            var solutionMoves = solution.get();

            writeSolutionSteps(solutionMoves, System.out::print);
            if (!solutionMoves.isEmpty()) {
                System.out.printf("!!! SOLVED in %d moves !!!%n", solutionMoves.size());
            } else {
                System.out.println("!!! ALREADY SOLVED !!!");
            }
            System.exit(EXIT_CODE_SUCCESS);
        } else {
            System.out.println("!!! NO SOLUTION FOUND !!!");
            System.exit(EXIT_CODE_NO_SOLUTION);
        }
    }

    private static void writeSolutionSteps(Collection<Move> moves, Consumer<String> out) {
        int step = 0;
        for (var move : moves) {
            step++;

            var puzzle = move.getBefore();
            var srcTubeIdx = move.getSrcTubeIdx();
            var dstTubeIdx = move.getDstTubeIdx();
            writePrettyPuzzleWithMovePointers(puzzle, srcTubeIdx, dstTubeIdx, out);

            out.accept(format("Step %d. Move from %d to %d%n%n",
                    step,
                    srcTubeIdx + 1,
                    dstTubeIdx + 1));
        }
    }

    private static Puzzle parsePuzzle(String[] serializedPuzzle) {
        var tubes = Arrays.stream(serializedPuzzle)
                .map(Tube::fromString)
                .collect(Collectors.toList());
        return Puzzle.withInitialState(tubes);
    }

    private static void validatePuzzle(Puzzle puzzle) {
        // puzzles with <= 2 tubes are _valid_, even if they're not solvable
        // puzzles with tubes with 0 capacity are _valid_, even if they might not be solvable
    }

    private static void writePrettyPuzzleWithMovePointers(
            Puzzle puzzle, int srcTubeIdx, int dstTubeIdx, Consumer<String> out) {

        for (int tubeIdx = 0; tubeIdx < puzzle.getTubeCount(); tubeIdx++) {
            if (tubeIdx == srcTubeIdx) {
                out.accept(dstTubeIdx < tubeIdx ? " « " : "   ");
                out.accept(" ⇑ ");
            } else if (tubeIdx == dstTubeIdx) {
                out.accept(srcTubeIdx < tubeIdx ? " » " : "   ");
                out.accept(" ⇓ ");
            } else if (tubeIdx > srcTubeIdx && tubeIdx < dstTubeIdx) {
                out.accept(" »  » ");
            } else if (tubeIdx > dstTubeIdx && tubeIdx < srcTubeIdx) {
                out.accept(" «  « ");
            } else {
                out.accept("      ");
            }
        }
        out.accept(System.lineSeparator());
        writePrettyPuzzle(puzzle, out);
    }

    private static void writePrettyPuzzle(Puzzle puzzle, Consumer<String> out) {
        // FIXME: for tubes of mixed capacities, other tubes are printed too tall
        var maxTubeCapacity = puzzle.streamTubes()//
                .mapToInt(Tube::getCapacity)//
                .max()//
                .orElse(0);
        for (int depth = 0; depth < maxTubeCapacity; depth++) {
            for (int tubeIdx = 0; tubeIdx < puzzle.getTubeCount(); tubeIdx++) {
                var tube = puzzle.getNthTube(tubeIdx);
                var fluidTypeStr = tube.getFluidTypeAtDepth(depth)
                        .map(Fluid.Type::toChar)
                        .orElse(' ');
                out.accept(format("   |%s|", fluidTypeStr));
            }
            out.accept(System.lineSeparator());
        }
        for (int tubeIdx = 0; tubeIdx < puzzle.getTubeCount(); tubeIdx++) {
            out.accept(format("   \\%d/", (tubeIdx + 1) % 10));
        }
        out.accept(System.lineSeparator());
    }
}
