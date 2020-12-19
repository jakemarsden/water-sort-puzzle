package com.github.jakemarsden.watersortpuzzle;

import java.util.*;
import java.util.stream.*;

import static java.lang.String.*;

public class Application {

    public static final int EXIT_CODE_SUCCESS = 0;
    public static final int EXIT_CODE_NO_SOLUTION = 1;
    public static final int EXIT_CODE_BAD_INPUT = 2;

    /**
     * Solve level #141 unless another puzzle is given as arguments.
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

    private static final boolean DEBUG = true;

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
            System.exit(EXIT_CODE_BAD_INPUT);
            return;
        }

        try {
            validatePuzzle(puzzle);
        } catch (IllegalArgumentException e) {
            System.out.println("!!! INVALID PUZZLE !!!");
            System.out.println(e.getLocalizedMessage());
            System.exit(EXIT_CODE_BAD_INPUT);
            return;
        }

        var solver = PuzzleSolver.forPuzzle(puzzle);
        var solution = solver.solve();

        if (solution.isPresent()) {
            var solutionMoves = solution.get();
            // TODO: Print solution moves
            System.out.println(!solutionMoves.isEmpty()//
                    ? format("!!! SOLVED in %d moves!!!", solutionMoves.size())//
                    : "!!! ALREADY SOLVED !!!");
            System.exit(EXIT_CODE_SUCCESS);

        } else {
            System.out.println("!!! NO SOLUTION FOUND !!!");
            System.exit(EXIT_CODE_NO_SOLUTION);
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
}
