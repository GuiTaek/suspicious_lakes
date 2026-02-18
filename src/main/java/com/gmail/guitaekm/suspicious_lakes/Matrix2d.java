package com.gmail.guitaekm.suspicious_lakes;

import java.util.List;

public record Matrix2d(int aa, int ab, int ba, int bb) {
    public Matrix2d multiply(Matrix2d other) {
        return new Matrix2d(
                this.aa * other.aa + this.ab * other.ba,
                this.aa * other.ab + this.ab * other.bb,
                this.ba * other.aa + this.bb * other.ba,
                this.ba * other.ab + this.bb * other.bb
        );
    }

    public static Matrix2d IDENTITY = new Matrix2d(
            1, 0,
            0, 1
    );
    public static Matrix2d SIMPLE_ROTATION = new Matrix2d(
            0, -1,
            1, 0
    );

    public Matrix2d inv() {
        return new Matrix2d(
                bb, -ab,
                -ba, aa
        );
    }

    public LakeDestinationFinder.GridPos multiply(LakeDestinationFinder.GridPos from) {
        return new LakeDestinationFinder.GridPos(
                this.aa * from.x() + this.ab * from.y(),
                this.ba * from.x() + this.bb * from.y()
        );
    }

    public static List<Matrix2d> ROTATIONS = List.of(
            IDENTITY,
            SIMPLE_ROTATION,
            SIMPLE_ROTATION.multiply(SIMPLE_ROTATION),
            SIMPLE_ROTATION.multiply(SIMPLE_ROTATION)
                    .multiply(SIMPLE_ROTATION)
    );
}
