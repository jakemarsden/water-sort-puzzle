package com.github.jakemarsden.watersortpuzzle;

import java.util.*;

import static java.util.Objects.*;

final class PuzzleSolver {

    public static PuzzleSolver forPuzzle(Puzzle puzzle) {
        requireNonNull(puzzle);
        return new PuzzleSolver(puzzle);
    }

    private final Puzzle puzzle;

    private PuzzleSolver(Puzzle puzzle) {
        this.puzzle = puzzle;
    }

    /**
     * @return <ul>
     *         <li>{@code Optional.empty()} => no solution</li>
     *         <li>{@code Optional.of({})} => already solved</li>
     *         <li>{@code Optional.of({...})} => moves to exec to solve the puzzle</li>
     *         </ul>
     */
    public Optional<? extends Collection<Move>> solve() {
        Queue<Move> solutionSteps = new ArrayDeque<>();
        boolean solved = solve(puzzle, solutionSteps);
        return solved ? Optional.of(solutionSteps) : Optional.empty();
    }

    private boolean solve(Puzzle puzzle, Queue<Move> solutionSteps) {
        if (puzzle.isSolved()) {
            return true;
        }

        for (int srcTubeIdx = 0; srcTubeIdx < puzzle.getTubeCount(); srcTubeIdx++) {
            for (int dstTubeIdx = 0; dstTubeIdx < puzzle.getTubeCount(); dstTubeIdx++) {
                // Can't move a tube to itself.
                if (dstTubeIdx == srcTubeIdx) {
                    continue;
                }

                var move = Move.of(puzzle, srcTubeIdx, dstTubeIdx);
                if (!move.isValid()) {
                    continue;
                }

                var nextPuzzleState = move.execute();
                if (solve(nextPuzzleState, solutionSteps)) {
                    solutionSteps.add(move);
                    return true;
                }
                // if this valid move did not ultimately solve the puzzle, continue looking for
                // other valid moves
            }
        }

        // We have tried every combination of moving liquids from one tube to another, either there
        // are no valid moves from this position, or all valid moves ultimately led to no solution.
        return false;
    }
}
