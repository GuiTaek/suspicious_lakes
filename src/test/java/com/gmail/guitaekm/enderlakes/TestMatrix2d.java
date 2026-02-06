package com.gmail.guitaekm.enderlakes;

import org.junit.jupiter.api.Test;

public class TestMatrix2d {
    @Test
    public void testMatrixRotations() {
        Matrix2d currRot = Matrix2d.IDENTITY;
        for (int rot = 0; rot < 5; rot++) {
            System.out.println(rot);
            System.out.println(currRot);
            System.out.println(Matrix2d.ROTATIONS.get(rot % 4));
            System.out.println();
            assert currRot.equals(Matrix2d.ROTATIONS.get(rot % 4));
            currRot = currRot.multiply(Matrix2d.SIMPLE_ROTATION);
        }
    }

    @Test
    public void testMatrixInv() {
        for (int rot = 0; rot < 4; rot++) {
            Matrix2d mat = Matrix2d.ROTATIONS.get(rot);
            assert mat.multiply(mat.inv()).equals(Matrix2d.IDENTITY);
        }
    }
}
