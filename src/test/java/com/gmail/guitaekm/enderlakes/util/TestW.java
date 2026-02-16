package com.gmail.guitaekm.enderlakes;

import com.gmail.guitaekm.enderlakes.util.W;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Random;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class TestW {
    public static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger("W_tester");

    // big number used for testing for approximately equal
    private static final double OMEGA = (int) 1e15;

    public static double randomDouble(Random random) {
        return (random.nextBoolean() ? +1 : -1) * (1 / random.nextDouble() - 1);
    }

    @Test
    public void WInvOfXExpSmall() {
        Random random = new Random(42);
        for (int i = 0; i < 10; i++) {
            double val = random.nextDouble(-1, 3 * Math.exp(3));
            double xExp = val * Math.exp(val);
            double valBack = W.apply(xExp);
            assertEquals(Math.round(val * OMEGA), Math.round(valBack * OMEGA), "%f was expected, %f was returned".formatted(val, valBack));
        }
    }

    @Test
    public void xExpInvOfWBig() {
        Random random = new Random(42);
        for (int i = 0; i < 1_000; i++) {
            double val = Math.abs(randomDouble(random)) - W.E_INV;
            double w = W.apply(val);
            double valBack = w * Math.exp(w);
            assertEquals(Math.round(val * OMEGA), Math.round(valBack * OMEGA), "%f was expected, %f was returned".formatted(val, valBack));
        }
    }

    public void runWRepetitionsTimes(int salt) {
        double sum = 0;
        for (long i = 0; i < REPETITIONS; i++) {
            sum += W.apply(i * i + salt);
        }
        // so it doesn't get optimized away
        LOGGER.info(String.valueOf(sum));
    }

    @Test
    public void testHardcodedValues() {
        double[] values = new double[] {-W.E_INV, 0d, W.E};
        for (double val : values) {
            double w = W.apply(val);
            double valBack = w * Math.exp(w);
            assertEquals(Math.round(val * OMEGA), Math.round(valBack * OMEGA),  "%f was expected, %f was returned".formatted(val, valBack));
        }
    }

    public static long MAX_TIME_NANOS_W = 500;
    public static long REPETITIONS = 2_000_000;

    @Test
    public void WFast() {
        // less than 1.5 seconds
        assert REPETITIONS * MAX_TIME_NANOS_W <= 1.5 * 1e9;

        // since this the i of the loop is used to generate unique numbers
        assert (double) REPETITIONS * (double) REPETITIONS < (double) Long.MAX_VALUE;

        runWRepetitionsTimes(0);
        long start = getCpuNanoTime();
        runWRepetitionsTimes(1);
        assert getCpuNanoTime() - start < MAX_TIME_NANOS_W * REPETITIONS;
    }

    public long getCpuNanoTime() {
        // see https://web.archive.org/web/20190923225132/http://nadeausoftware.com/articles/2008/03/java_tip_how_get_cpu_and_user_time_benchmarking
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        if (bean.isCurrentThreadCpuTimeSupported()) {
            return bean.getCurrentThreadCpuTime();
        } else {
            LOGGER.warn("currentThreadCpuTime is not supported");
            return System.currentTimeMillis() * 1_000;
        }
    }
}
