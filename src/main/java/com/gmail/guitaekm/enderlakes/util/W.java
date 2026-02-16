package com.gmail.guitaekm.enderlakes.util;

import java.util.HashMap;
import java.util.Map;

/**
 * implements the lambert W function
 */
public class W {
    static {

    }
    public static final double E = Math.exp(1);
    public static final double E_INV = 1 / E;
    private static final double EPSILON = 1e-15d;
    private static final double LN_2 = Math.log(2);

    private static final Map<Double, Double> hardcordedValues = Map.of(
            -E_INV, -1d,
            0d, 0d,
            E, 1d
    );

    /**
     * used in nrIterations. Here, the number of iterations is calculated (it is constant)
     * See for the formulas in method W(double)
     */
    private static final Map<Double, Integer> rawNrIterations;
    static {
        rawNrIterations = new HashMap<>();
        rawNrIterations.put(
                0d,
                // inverse of 0.1^(2^n) = EPSILON
                (int) Math.ceil(Math.log(Math.log(EPSILON) / Math.log(0.1)) / LN_2)
        );
        rawNrIterations.put(
                E,
                // inverse of (1 - E_INV)^(2^n - 1) / 5 = EPSILON
                (int) Math.ceil(Math.log(Math.log(5 * EPSILON) / Math.log1p(-E_INV)) / LN_2)
        );
        rawNrIterations.put(
                Double.POSITIVE_INFINITY,
                // inverse of (log1p(E_INV)^(2^n) = EPSILON
                (int) Math.ceil(Math.log(Math.log(EPSILON) / Math.log(Math.log1p(E_INV))) / LN_2)
        );
    }

    /**
     * nr of iterations needed for a precision of EPSILON
     */
    private static final IntervalFunction<Integer> nrIterations = new IntervalFunction<>(
            -E_INV,
            Map.of(
                    0d, x -> rawNrIterations.get(0d),
                    E, x -> rawNrIterations.get(E),
                    Double.POSITIVE_INFINITY, x -> rawNrIterations.get(Double.POSITIVE_INFINITY)
            ),
            "nrIterations"
    );

    /** initial value for the method given used in W(double)
     *  there for the method that is used.
     */
    private static final IntervalFunction<Double> w0 = new IntervalFunction<>(
            -E_INV,
            Map.of(
                    0d, x -> {
                        double eX = E * x;
                        // with "complex" I don't mean complex number but the opposite of "simple"
                        double complexTerm = 1 + eX;
                        double complexSqrt = Math.sqrt(complexTerm);
                        return eX * Math.log1p(complexSqrt) / (complexTerm + complexSqrt);
                    },
                    E, x -> x / E,
                    Double.POSITIVE_INFINITY, x -> {
                        double logX = Math.log(x);
                        return logX - Math.log(logX);
                    }
            ),
            "w0"
    );

    /** the  <a href="https://en.wikipedia.org/wiki/Lambert_W_function">lambert W function</a>. Optimized for correctness
     *
     * @param x the parameter of the function
     * @return the function value
     */
    public static double apply(double x) {
        // see https://en.wikipedia.org/wiki/Lambert_W_function#Numerical_evaluation
        // I decided for the mathematical save solution because quadratic-rate is already a lot
        // and I prefer safety here

        // the theorem makes no statement about the border values, therefore I have to hardcode them
        if (W.hardcordedValues.containsKey(x)) {
            return W.hardcordedValues.get(x);
        }
        double wn = W.w0.apply(x);
        for (int i = 0; i < W.nrIterations.apply(x); i++) {
            wn = wn / (1 + wn) * (1 + Math.log(x / wn));
        }
        return wn;
    }
}
