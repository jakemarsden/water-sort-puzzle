package com.github.jakemarsden.watersortpuzzle;

import static java.util.Objects.*;

final class Move {

    public static Move of(Puzzle before, int srcTubeIdx, int dstTubeIdx) {
        requireNonNull(before, "before");
        if (srcTubeIdx < 0 || srcTubeIdx >= before.getTubeCount()) {
            throw new IllegalArgumentException("srcTubeIdx: " + srcTubeIdx);
        }
        if (dstTubeIdx < 0 || dstTubeIdx >= before.getTubeCount()) {
            throw new IllegalArgumentException("dstTubeIdx: " + srcTubeIdx);
        }
        return new Move(before, srcTubeIdx, dstTubeIdx);
    }

    private final Puzzle before;
    private final int srcTubeIdx;
    private final int dstTubeIdx;

    private Move(Puzzle before, int srcTubeIdx, int dstTubeIdx) {
        this.before = before;
        this.srcTubeIdx = srcTubeIdx;
        this.dstTubeIdx = dstTubeIdx;
    }

    /**
     * @return {@code true} if this move can be applied to the given puzzle
     */
    public boolean isValid() {
        // f) Not allowed to move tube to itself
        if (srcTubeIdx == dstTubeIdx) {
            return false;
        }

        var src = before.getNthTube(srcTubeIdx);
        var dst = before.getNthTube(dstTubeIdx);

        // e) Not allowed to move the contents from an empty tube
        if (src.isEmpty()) {
            return false;
        }
        var srcFluid = src.getTopFluid().orElseThrow(IllegalStateException::new);

        // a) Not allowed to move entire contents of a tube to an empty tube (this could create
        //  infinite loops with liquids being bounced backwards and forwards)
        if (src.containsSingleFluidType() && dst.isEmpty()) {
            return false;
        }

        // b) Not allowed to move more liquid than will fit into the target tube
        //  FIXME: Actually, splitting fluids may be valid in some circumstances
        if (srcFluid.getAmount() > dst.getSpareCapacity()) {
            return false;
        }

        // c) Can only move a liquid to an empty tube or on top of a liquid of the same colour
        if (!dst.isEmpty() && srcFluid.getType() != dst.getTopFluid().orElseThrow().getType()) {
            return false;
        }

        // d) Must move as much liquid as there is of one colour from the from tube

        return true;
    }

    /**
     * Returns a new {@link Puzzle} representing the given puzzle with this move
     * applied. <strong>No effort is made to actually {@link #isValid()
     * validate} the move.</strong>
     */
    public Puzzle execute() {
        if (srcTubeIdx == dstTubeIdx) {
            // no-op, avoid the copy
            return before;
        }

        var maybeFluidToMove = before.getNthTube(srcTubeIdx).getTopFluid();
        if (maybeFluidToMove.isEmpty()) {
            // moving empty tube is no-op, avoid the copy
            return before;
        }
        var fluidToMove = maybeFluidToMove.get();

        return before//
                .withUpdatedTube(srcTubeIdx, Tube::withoutTopFluid)//
                .withUpdatedTube(dstTubeIdx, dst -> dst.withAdditionalFluid(fluidToMove));
    }
}
