package com.gmail.guitaekm.suspicious_lakes;

import com.gmail.guitaekm.suspicious_lakes.util.IntervalFunction;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class TestIntervalFunction {
    @Test
    public void testFiniteCorrectness() {
        IntervalFunction<Double> function = new IntervalFunction<Double>(
                -1d,
                Map.of(
                        1d, x -> x,
                        2d, x-> 2 * x,
                        3d, x-> 3 * x
                ),
                "function"
        );
        assertThrows(IllegalArgumentException.class, () -> function.apply(-1.001));
        assertEquals(-1.0, function.apply(-1.0));
        assertEquals(-0.5, function.apply(-0.5));
        assertEquals(0.0, function.apply(0.0));
        assertEquals(1.0, function.apply(1.0));
        assertEquals(3.0, function.apply(1.5));
        assertEquals(4.0, function.apply(2.0));
        assertEquals(7.5, function.apply(2.5));
        assertEquals(9.0, function.apply(3.0));
        assertThrows(IllegalArgumentException.class, () -> function.apply(3.001));
    }

    @Test
    public void testInfiniteCorrectness() {
        IntervalFunction<Double> function = new IntervalFunction<>(
                Double.NEGATIVE_INFINITY,
                Map.of(
                        0d, x -> -2 * x,
                        Double.POSITIVE_INFINITY, x -> +2 * x
                ),
                "infiniteTest"
        );

        // a test if -Double.MAX_VALUE is really the largest negative value as suggested by https://stackoverflow.com/questions/2389613/correct-way-to-obtain-the-most-negative-double
        Random random = new Random(42);
        for (int i = 0; i < 1_000; i ++) {
            assert -Double.MAX_VALUE < 1 / random.nextDouble(1e-306);
        }
        // technically Intellij said this would always pass, but I don't trust Intellij handling overflow correctly
        //noinspection ConstantValue
        assert -Double.MAX_VALUE < -1e308;
        //noinspection ConstantValue
        assert -Double.MAX_VALUE != Double.NEGATIVE_INFINITY;

        for (int i = 0; i < 1_000; i++) {
            double val = TestW.randomDouble(random);
            assertDoesNotThrow(() -> function.apply(val));
            assertEquals(2 * Math.abs(val), function.apply(val));
        }


        assertDoesNotThrow(() -> function.apply(-Double.MAX_VALUE));
        assertEquals(Double.MAX_VALUE, function.apply(-Double.MAX_VALUE / 2));

        assertDoesNotThrow(() -> function.apply(Double.MAX_VALUE));
        assertEquals(Double.MAX_VALUE, function.apply(Double.MAX_VALUE / 2));
    }
}
