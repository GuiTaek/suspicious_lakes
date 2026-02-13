package com.gmail.guitaekm.enderlakes;

import com.gmail.guitaekm.enderlakes.LakeDestinationFinder.GridPos;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.minecraft.util.math.ChunkPos;

import static org.junit.jupiter.api.Assertions.*;

public class TestLakeDestinationFinder {
    public static final Logger LOGGER = LoggerFactory.getLogger("lake_destination_tester");
    static ConfigInstance smallPrimeConfig = new ConfigInstance(
            19,
            ConfigValues.powerDistance,
            List.of(
                    1, 4, 3, 4, 0, 6
            ),
            ConfigValues.minimumDistance,
            new int[] {2, 3, 3},
            100
    );
    final private static ConfigInstance CONFIG =  new ConfigInstance();

    static {
        int nrLakes = LakeDestinationFinder.findNewNrLakes(CONFIG, 30_000, -1);
        int[] factsPhi = LakeDestinationFinder.primeFactors(nrLakes - 1)
                .stream()
                .mapToInt(i -> i)
                .toArray();

        MIDDLE_CONFIG = new ConfigInstance(
                nrLakes,
                CONFIG.powerDistance(),
                CONFIG.cycleWeights(),
                CONFIG.minimumDistance(),
                factsPhi,
                64
        );
    }
    final private static ConfigInstance MIDDLE_CONFIG;

    @Test
    public void testPi() {
        List<Integer> piIVals = List.of(
                0,

                2, 1,
                4, 3,

                6, 7, 5,

                9, 10, 11, 8,

                13, 14, 15, 16, 17, 12
        );
        int ind = 0;
        for (Integer piI : piIVals)
            assertEquals(piI, LakeDestinationFinder.pi(ind++, smallPrimeConfig));
    }
    @Test
    public void testCInjective() {
        Set<GridPos> usedPositions = new HashSet<>();
        for (int i: IntStream.rangeClosed(1, 1000).toArray()) {
            GridPos outp = LakeDestinationFinder.c(i);
            assertFalse(usedPositions.contains(outp), outp.toString());
            usedPositions.add(outp);
        }
    }
    @Test void testCInvInjective() {
        Set<Integer> usedIntegers = new HashSet<>();
        for (int x = -100; x <= 100; x++) {
            for (int y = -100; y <= 100; y++) {
                if (x == 0 && y == 0) {
                    continue;
                }
                int i = LakeDestinationFinder.cInv(+x, +y);
                assertFalse(usedIntegers.contains(i), String.valueOf(i));
                usedIntegers.add(i);
            }
        }
    }
    @Test void testGetRotation() {
        testRotationWithXAndY(1, 0);
        testRotationWithXAndY(1, 1);
    }
    public static void testRotationWithXAndY(int x, int y) {
        assertEquals(0, LakeDestinationFinder.getRotation(+x, +y));
        assertEquals(1, LakeDestinationFinder.getRotation(-y, +x));
        assertEquals(2, LakeDestinationFinder.getRotation(-x, -y));
        assertEquals(3, LakeDestinationFinder.getRotation(+y, -x));
    }
    @Test
    public void CInvPositive() {
        for (int x = -100; x <= 100; x++) {
            for (int y = -100; y <= 100; y++) {
                if (x == 0 && y == 0) {
                    continue;
                }
                Assertions.assertTrue(LakeDestinationFinder.cInv(x, y) >= 1);
            }
        }
    }

    @Test
    public void cThrows() {
        for(int i = -1000; i < 0; i++) {
            final int giveI = i;
            assertThrows(
                    IllegalArgumentException.class,
                    () -> LakeDestinationFinder.c(giveI)
            );
        }
    }

    @Test
    public void cInvThrows() {
        assertThrows(
                IllegalArgumentException.class,
                () -> LakeDestinationFinder.cInv(0, 0)
        );
    }

    @Test
    public void cInvInvOfC() {
        for (int i = 1; i < 10_000; i++) {
            assertEquals(i, LakeDestinationFinder.cInv(
                    LakeDestinationFinder.c(i)
            ));
        }
    }

    @Test
    public void testGetRotationManual() {
        assert LakeDestinationFinder.getRotation(1, 0) == 0;
        assert LakeDestinationFinder.getRotation(1, 1) == 0;
        assert LakeDestinationFinder.getRotation(0, 1) == 1;
        assert LakeDestinationFinder.getRotation(-1, 1) == 1;
        assert LakeDestinationFinder.getRotation(-1, 0) == 2;
        assert LakeDestinationFinder.getRotation(-1, -1) == 2;
        assert LakeDestinationFinder.getRotation(0, -1) == 3;
        assert LakeDestinationFinder.getRotation(1, -1) == 3;
    }

    @Test
    public void testGetRotationAutomatic() {
        for (int radius = 1; radius < 50; radius++) {
            int finalRadius = radius;
            IntStream.rangeClosed(-radius + 1, +radius)
                    .mapToObj(y -> new GridPos(finalRadius, y))
                    .forEach(
                            pos -> {
                                for (int rot = 0; rot < 4; rot++) {
                                    GridPos newPos = Matrix2d.ROTATIONS.get(rot).multiply(pos);
                                    assert LakeDestinationFinder.getRotation(newPos) == rot;
                                }
                            }
                    );
        }
    }

    @Test
    public void testCSurjective() {
        int RADIUS = 10;
        int DIAMETER = 2 * RADIUS + 1;
        boolean[][] gridFlags = new boolean[DIAMETER][DIAMETER];
        for (int i = 1; i < DIAMETER * DIAMETER; i++) {
            GridPos pos = LakeDestinationFinder.c(i);
            int offX = pos.x() + RADIUS;
            int offY = pos.y() + RADIUS;
            assert offX <= DIAMETER;
            assert offY <= DIAMETER;
            gridFlags[offX][offY] = true;
        }
        assert !gridFlags[RADIUS][RADIUS];
        for (int offX = 0; offX < DIAMETER; offX++) {
            if (offX != RADIUS) {
                for (int offY = 0; offY < DIAMETER; offY++) {
                    assert gridFlags[offX][offY];
                }
                continue;
            }
            for (int offY = 0; offY <= RADIUS - 1; offY++) {
                assert gridFlags[offX][offY];
            }
            for (int offY = RADIUS + 1; offY < DIAMETER; offY++) {
                assert gridFlags[offX][offY];
            }
        }
    }

    @Test
    public void testCOnlyIncrease() {
        int maxCoord = -1;
        for (int i = 1; i < 10_000; i++) {
            GridPos pos = LakeDestinationFinder.c(i);
            int newCoord = Math.max(Math.abs(pos.x()), Math.abs(pos.y()));
            assert maxCoord <= newCoord;
            maxCoord = newCoord;
        }

    }

    public void testInteger(ConfigInstance config, int val, boolean isSafe) {
        GridPos gPos = LakeDestinationFinder.c(val);
        ChunkPos pos = LakeDestinationFinder.rawPosUnsafe(config, gPos.x(), gPos.y());
        assert isSafe == LakeDestinationFinder.isSafeChunk(config, pos);
    }

    @Test
    public void testLastUnsafeIntegerIsReallyLast() {
        for (int lastUnsafeChunk = 10; lastUnsafeChunk < 100; lastUnsafeChunk++) {
            ConfigInstance config = new ConfigInstance(
                    CONFIG.nrLakes(),
                    CONFIG.powerDistance(),
                    CONFIG.cycleWeights(),
                    CONFIG.minimumDistance(),
                    CONFIG.factsPhi(),
                    lastUnsafeChunk
            );
            int o = LakeDestinationFinder.lastUnsafeInteger(config);
            for (int val = Math.max(1, o - 100); val <= o; val++) {
                testInteger(config, val, false);
            }
            for (int val = o + 1; val <= o + 100; val++) {
                testInteger(config, val, true);
            }
        }
    }

    @Test
    public void fInvInvOffF() {
        // this is approximately the whole range f is meant to work on
        for (int c = -350; c <= 350; c++) {
            assertEquals(
                    c, LakeDestinationFinder.fInv(
                        CONFIG,
                        LakeDestinationFinder.fRound(CONFIG, c)
                    )
            );
        }
    }

    public void testFFloorRoundCeil(ConfigInstance config) {
        for (int c = -300; c <= 300; c++) {
            int fFloor = LakeDestinationFinder.fFloor(config, c);
            double fRaw = LakeDestinationFinder.fRaw(config, c);
            int fRound = LakeDestinationFinder.fRound(config, c);
            int fCeil = LakeDestinationFinder.fCeil(config, c);
            Set<Integer> signums = new HashSet<>();
            signums.add(Integer.compare(fFloor, 0));
            signums.add(Double.compare(fRaw, 0));
            signums.add(Integer.compare(fRound, 0));
            signums.add(Integer.compare(fCeil, 0));
            assert signums.size() == 1;
            assert signums.stream().findFirst().get().equals(Integer.compare(c, 0));

            fFloor = Math.abs(fFloor);
            fRaw = Math.abs(fRaw);
            fRound = Math.abs(fRound);
            fCeil = Math.abs(fCeil);

            assert fFloor <= fRaw && fRaw  <= fCeil;
            assert fFloor <= fRound && fRound <= fCeil;
        }
    }

    @Test
    public void testFFloorRoundCeil() {
        testFFloorRoundCeil(CONFIG);
        testFFloorRoundCeil(MIDDLE_CONFIG);
        testFFloorRoundCeil(smallPrimeConfig);
    }

    public void testFInvRawIntegerWhenCeilIsEqual(ConfigInstance config, int val) {
        int processedValue = LakeDestinationFinder.fCeil(config, LakeDestinationFinder.fInvCeil(config, val));

        // in words: when val == f(fInvCeil(val)), fInvRaw(val) must be integer
        assert processedValue != val || LakeDestinationFinder.fInvRaw(config, val) % 1 == 0.0;
    }

    public void testFInvRawIntegerWhenCeilIsEqual(ConfigInstance config) {
        // this test was somehow difficult to craft and catches a bug I had in f
        Random random = new Random(42);
        for (int smallVal = 5; smallVal < 1_000; smallVal++) {
            int randVal = random.nextInt(10_000);
            testFInvRawIntegerWhenCeilIsEqual(config, randVal);
            testFInvRawIntegerWhenCeilIsEqual(config, smallVal);
        }
    }

    @Test
    public void testFInvRawIntegerWhenCeilIsEqual() {
        testFInvRawIntegerWhenCeilIsEqual(CONFIG);
        testFInvRawIntegerWhenCeilIsEqual(MIDDLE_CONFIG);
        testFInvRawIntegerWhenCeilIsEqual(smallPrimeConfig);
    }

    @Test
    public void fInvFloorBelowF() {
        for (int c = -350; c <= 350; c++) {
            if (Math.abs(c) < 2) {
                continue;
            }
            int fC = LakeDestinationFinder.fRound(CONFIG, c);
            int signum = Integer.compare(fC, 0);
            assertEquals(c - signum, LakeDestinationFinder.fInvFloor(CONFIG, fC - signum));
            assertEquals(c, LakeDestinationFinder.fInvFloor(CONFIG, fC + signum));
        }
    }

    @Test
    public void fInvRoundF() {
        for (int c = -350; c <= 350; c++) {
            if (Math.abs(c) < 2) {
                continue;
            }
            int fC = LakeDestinationFinder.fRound(CONFIG, c);
            int signum = Integer.compare(fC, 0);
            assertEquals(c, LakeDestinationFinder.fInv(CONFIG, fC - signum));
            assertEquals(c, LakeDestinationFinder.fInv(CONFIG, fC + signum));
        }
    }

    @Test
    public void fInvCeilAboveF() {
        for (int c = -350; c <= 350; c++) {
            if (Math.abs(c) < 2) {
                continue;
            }
            int fC = LakeDestinationFinder.fRound(CONFIG, c);
            int signum = Integer.compare(fC, 0);
            assertEquals(c, LakeDestinationFinder.fInvCeil(CONFIG, fC - signum));
            assertEquals(c + signum, LakeDestinationFinder.fInvCeil(CONFIG, fC + signum));
        }
    }

    @Test
    public void rawGridPosInvOfRawPos() {
        for (int x = -100; x <= 100; x++) {
            for (int y = -100; y <= 100; y++) {
                try {
                    GridPos oldPos = new GridPos(x, y);
                    GridPos newPos = LakeDestinationFinder.getRawGridPos(
                            CONFIG,
                            LakeDestinationFinder.rawPos(CONFIG, oldPos)
                    );
                    assertEquals(oldPos, newPos);
                } catch (IllegalArgumentException ignored) { }
            }
        }
    }

    @Test
    public void nearestLakeInvOfPos() {
        Random rand = new Random(42);
        for (int i = 0; i < 10; i++) {
            nearestLakeInvOfPosWithSeed(rand.nextLong());
        }
    }

    public static void nearestLakeInvOfPosWithSeed(long seed) {
        for (int x = -50; x <= 50; x++) {
            for (int y = -50; y <= 50; y++) {
                GridPos oldPos = new GridPos(x, y);
                ChunkPos rawChunkPos;
                try {
                    rawChunkPos = LakeDestinationFinder.pos(CONFIG, seed, oldPos);
                } catch (IllegalArgumentException exc) {
                    continue;
                }
                Set<GridPos> newPosses = LakeDestinationFinder.findNearestLake(
                        CONFIG,
                        seed,
                        rawChunkPos
                );
                assertTrue(newPosses.contains(oldPos));
            }
        }
    }

    @Test
    public void nearestLakeIsIndeedNearest() {
        Random rand = new Random(42);
        for (int i = 0; i < 10; i++) {
            nearestLakeIsIndeedNearestWithSeed(rand.nextLong());
        }
    }

    public static void nearestLakeIsIndeedNearestWithSeed(long seed) {
        // 350 is approximately the biggest needed grid coordinate
        // define a range, leave enough tolerances for checking really every lake position
        // have to be confined so it is possible to run this efficiently
        ChunkPos from = LakeDestinationFinder.rawPos(CONFIG, new GridPos(-50, -50));
        ChunkPos to = LakeDestinationFinder.rawPos(CONFIG, new GridPos(50, 50));
        Random rand = new Random(seed);
        int xCheck = rand.nextInt(from.x, to.x);
        int zCheck = rand.nextInt(from.z, to.z);
        ChunkPos checkChunk = new ChunkPos(xCheck, zCheck);
        // overflows when being integer
        long bestDistance = Long.MAX_VALUE;
        Set<ChunkPos> bestChunks = new HashSet<>();
        for (int x = -100; x <= 100; x++) {
            for (int y = -100; y <= 100; y++) {
                ChunkPos currChunk;
                try {
                    currChunk = LakeDestinationFinder.pos(CONFIG, seed, new GridPos(x, y));
                } catch (RuntimeException exc) {
                    continue;
                }
                int dx = currChunk.x - xCheck;
                int dz = currChunk.z - zCheck;
                long currDistSq = (long) dx * dx + (long) dz * dz;
                if (currDistSq == bestDistance) {
                    bestChunks.add(currChunk);
                }
                if (currDistSq < bestDistance) {
                    bestDistance = currDistSq;
                    bestChunks = new HashSet<>();
                    bestChunks.add(currChunk);
                }
            }
        }
        Set<ChunkPos> toCheck = LakeDestinationFinder.findNearestLake(
                        CONFIG,
                        seed,
                        checkChunk
                ).stream()
                .map(gridPos -> LakeDestinationFinder.pos(CONFIG, seed, gridPos))
                .collect(Collectors.toSet());
        assertTrue(bestChunks.containsAll(toCheck) && toCheck.containsAll(bestChunks));

    }

    @Test
    public void testModularExponentiationBySquaring() {
        testModularMultiplicationAndExponentiation(
                LakeDestinationFinder::modularExponentiationBySquaring,
                (a, b) -> (int)(Math.round(Math.pow(a, b))),
                Math::pow
        );
    }

    @Test void testModularMultiplicationByDoubling() {
        testModularMultiplicationAndExponentiation(
                LakeDestinationFinder::modularMultiplicationByDoubling,
                (a, b) -> a * b,
                (a, b) -> a * b
        );
    }

    public interface TernaryOperator<T> {
        T apply(T a, T b, T c);
    }

    public void testModularMultiplicationAndExponentiation(
            TernaryOperator<Integer> testedOperation,
            BinaryOperator<Integer> overflowUnsafeOperation,
            BinaryOperator<Double> unpreciseOperation
            ) {
        Random random = new Random(42);
        int counter = 0;
        for (int i = 0; i < 10000; i++) {
            int b = random.nextInt(-20, 20);
            int e = random.nextInt(2, 8);
            if (unpreciseOperation.apply((double)b, (double)e) >= Integer.MAX_VALUE) {
                continue;
            }
            counter++;
            int n = random.nextInt(Math.max(Math.abs(b) + 1, 2), 100);
            int expected = (overflowUnsafeOperation.apply(b, e) % n + n) % n;
            long actual = testedOperation.apply(b, e, n);
            assertEquals(expected, actual);
        }
        assert counter >= 1000;
    }

    @Test void testIsPrimitiveRootIsFast() {
        int counter = 0;
        int n = CONFIG.nrLakes();
        for (int i = 0; i < 100; i++) {
            int g = new Random().nextInt(CONFIG.nrLakes());
            if (LakeDestinationFinder.isPrimitiveRootFast(g, n, CONFIG.factsPhi())) {
                counter++;
            }
        }
        assert counter >= 20;
    }

    @Test
    public void testIsPrimitiveRoot() {
        Random random = new Random(42);
        // now that the prime number is so big, this code isn't possible to be run through because the
        // memory that isPrimitiveRootSlow needs is so inefficient (it was optimized to be mathematically
        // exact, not fast or memory efficient)
        /*
        {
            int n = CONFIG.nrLakes();
            for (int smallVal = 0; smallVal < 1; smallVal++) {
                int g = random.nextInt(2, n);
                boolean expected = isPrimitiveRootSlow(g, n);
                boolean actual = LakeDestinationFinder.isPrimitiveRootFast(g, n, CONFIG.factsPhi());
                assertEquals(expected, actual);
            }
        }
         */
        for (int i = 0; i < 1_000; i++) {
            int g = random.nextInt(2, 1000);
            int n = random.nextInt(g + 1, 2000);
            while (!BigInteger.valueOf(n).isProbablePrime(1_000)) n++;
            int[] factsPhi = LakeDestinationFinder.primeFactors(n - 1).stream().mapToInt(fact -> fact).toArray();
            assertEquals(isPrimitiveRootSlow(g, n), LakeDestinationFinder.isPrimitiveRootFast(g, n, factsPhi));
        }
    }

    public static boolean isPrimitiveRootSlow(int g, int n) {
        assert LakeDestinationFinder.isPrime(n);
        Set<Integer> result = new HashSet<>();
        for (int i = 0; i < n; i++) {
            result.add(LakeDestinationFinder.modularExponentiationBySquaring(g, i, n));
        }
        return result.size() == n - 1;
    }

    @Test
    public void testIsPrime() {
        List<Integer> firstPrimesList =
            List.of(
                    2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37,
                    41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97
            );
        int lastPrime = firstPrimesList.getLast();
        Set<Integer> firstPrimes = new HashSet<>(firstPrimesList);
        for (int i = 0; i <= lastPrime; i++) {
            boolean expected = firstPrimes.contains(i);
            boolean actual = LakeDestinationFinder.isPrime(i);
            assertEquals(expected, actual);
        }
    }

    @Test
    public void testPiCycles() {
        Random random = new Random(42);
        testAbstractCycles(
                () -> random.nextInt(1, CONFIG.nrLakes()),
                i -> LakeDestinationFinder.pi(i, CONFIG),
                Objects::equals,
                1_000
        );
    }

    @Test
    public void testGridTeleportAim() {
        Random random = new Random(42);
        for (int i = 0; i < 3; i++) {
            int g = LakeDestinationFinder.calculateG(CONFIG.nrLakes(), CONFIG.factsPhi(), random.nextLong());
            int gInv = LakeDestinationFinder.calculateInv(CONFIG.nrLakes(), g);
            int boundary = LakeDestinationFinder.fInv(CONFIG, (int)(Math.sqrt(CONFIG.nrLakes()) + 0.5));
            testAbstractCycles(
                    () -> {
                        int coord1 = random.nextInt(-boundary, boundary);
                        int coord2 = random.nextInt(1, boundary);
                        if (random.nextBoolean()) {
                            coord2 = -coord2;
                        }
                        int x, z;
                        if (random.nextBoolean()) {
                            x = coord1;
                            z = coord2;
                        } else {
                            x = coord2;
                            z = coord1;
                        }
                        return new GridPos(x, z);
                    },
                    oldPos -> LakeDestinationFinder.teleportAim(
                            CONFIG,
                            oldPos,
                            g,
                            gInv
                    ),
                    (pos1, pos2) -> pos1.x() == pos2.x() && pos1.y() == pos2.y(),
                    30
            );
        }
    }

    @Test
    public void reproduceOverflowBug() {
        int gInv = 234_200_297;
        int pi = 1_026_132_924;
        int nrLakes = 1_107_891_224;
        assert LakeDestinationFinder.modularMultiplicationByDoubling(gInv, pi, nrLakes) > 0;
    }

    @Test
    public void testMultiplicationPositive() {
        Random random = new Random(42);
        for (int N = 100; N < 10_000; N++) {
            while (!BigInteger.valueOf(N).isProbablePrime(1_000)) {
                N++;
            }
            int a = random.nextInt(1, N);
            int b = random.nextInt(1, N);
            assert LakeDestinationFinder.modularMultiplicationByDoubling(a, b, N) > 0;
        }
    }

    public <T> void testAbstractCycles(
            Supplier<T> startElements,
            UnaryOperator<T> advancer,
            BiFunction<T, T, Boolean> isEqual,
            int repetitions
    ) {
        // records don't have a proper hashCode
        // methods therefore a Set can't be used
        List<T> previousElements = new ArrayList<>();
        T currentElement = startElements.get();
        for (int i = 0; i < repetitions; i++) {
            List<T> otherElements = previousElements.size() >= 2
                    ? previousElements.subList(1, previousElements.size())
                    : List.of();
            for (T previousElement : otherElements) {
                if (isEqual.apply(currentElement, previousElement)) {
                    LOGGER.error(
                            "{} is equal to {} but should instead be equal to {}",
                            currentElement,
                            previousElement,
                            previousElements.getFirst()
                    );
                    assert false;
                }
            }
            if (!previousElements.isEmpty() && isEqual.apply(currentElement, previousElements.getFirst())) {
                previousElements.clear();
                currentElement = startElements.get();
            }
            if (previousElements.size() > CONFIG.cycleWeights().size()) {
                LOGGER.error(String.valueOf(previousElements));
                assert false;
            }
            previousElements.add(currentElement);
            currentElement = advancer.apply(currentElement);

        }
    }

    @Test
    public void testFindNewLakes() {
        for (ConfigInstance config : List.of(smallPrimeConfig, CONFIG)) {
            for (int border = 5_000; border <= 100_000; border += 1_000) {
                {
                    int nrLakes = LakeDestinationFinder.findNewNrLakes(config, border, -1);
                    int lastLake = nrLakes - 1;
                    ChunkPos lastChunk = LakeDestinationFinder.rawPos(config, LakeDestinationFinder.c(lastLake));
                    BlockPos lastPos = lastChunk.getBlockPos(8, 0, 8);
                    assert Math.abs(lastPos.getX()) <= border / 2;
                    assert Math.abs(lastPos.getY()) <= border / 2;
                }
                {
                    int nrLakes = LakeDestinationFinder.findNewNrLakes(config, border, +1);
                    int lastLake = nrLakes - 1;
                    ChunkPos lastChunk = LakeDestinationFinder.rawPos(config, LakeDestinationFinder.c(lastLake));
                    BlockPos lastPos = lastChunk.getBlockPos(8, 0, 8);
                    assert Math.abs(lastPos.getX()) > border / 2 || Math.abs(lastPos.getZ()) > border / 2;
                }
            }
        }
    }

    public int getNumberFromChunk(ConfigInstance config, ChunkPos chunkPos) {
        GridPos gridPos = LakeDestinationFinder.getRawGridPos(config, chunkPos);
        return LakeDestinationFinder.cInv(gridPos);

    }

    public void testLastPos(ConfigInstance config) {
        Random random = new Random(42);
        for (int i = 0; i < 10_000; i++) {
            int range = random.nextInt(1, 1_000);
            ChunkPos chunkPos = LakeDestinationFinder.lastPos(config, new ChunkPos(range, range));
            GridPos gridPos = LakeDestinationFinder.getRawGridPos(config, chunkPos);
            int toCheckNumber = LakeDestinationFinder.cInv(gridPos);
            for (int x = -range; x <= +range; x++) {
                assert toCheckNumber >= getNumberFromChunk(config, new ChunkPos(x, +range));
                assert toCheckNumber >= getNumberFromChunk(config, new ChunkPos(x, -range));
            }
            for (int y = -range; y <= +range; y++) {
                assert toCheckNumber >= getNumberFromChunk(config, new ChunkPos(+range, y));
                assert toCheckNumber >= getNumberFromChunk(config, new ChunkPos(-range, y));
            }
        }
    }

    @Test
    public void testLastPos() {
        testLastPos(CONFIG);
        testLastPos(MIDDLE_CONFIG);
        testLastPos(smallPrimeConfig);
    }

    @Test
    public void testGInv() {
        Random random = new Random(42);
        for (int i = 0; i < 1_000; i++) {
            int N = random.nextInt(2, 10_000);
            while (!LakeDestinationFinder.isPrime(N)) {
                N++;
            }
            int[] phiFacts = LakeDestinationFinder
                    .primeFactors(N - 1)
                    .stream()
                    .mapToInt(fact -> fact)
                    .toArray();
            int g = LakeDestinationFinder.calculateG(N, phiFacts, random.nextLong());
            int gInv = LakeDestinationFinder.calculateInv(N, g);
            assert LakeDestinationFinder.modularMultiplicationByDoubling(g, gInv, N) == 1;
        }
    }

    public void testGIsPrimitiveRoot(ConfigInstance config, long seed) {
        int g = LakeDestinationFinder.getG(CONFIG.nrLakes(), CONFIG.factsPhi(), seed);
        assert LakeDestinationFinder.isPrimitiveRootFast(g, CONFIG.nrLakes(), CONFIG.factsPhi());
    }

    @Test
    public void testGIsPrimitiveRoot() {
        Random random = new Random(42);
        for (int i = 0; i < 1000; i++) {
            testGIsPrimitiveRoot(CONFIG, random.nextLong());
            testGIsPrimitiveRoot(smallPrimeConfig, random.nextLong());
        }
    }

    public void testTeleportAimNeverUnsafeWithConfig(Random random, ConfigInstance config) {
        for (int i = 0; i < 1_000; i++) {
            int x = random.nextInt(1, config.nrLakes());
            int z = random.nextInt(config.nrLakes());
            if (random.nextBoolean()) {
                x = -x;
            }
            if (random.nextBoolean()) {
                z = -z;
            }
            if (random.nextBoolean()) {
                int temp = x;
                x = z;
                z = temp;
            }
            long seed = random.nextLong();
            int g = LakeDestinationFinder.getG(config.nrLakes(), config.factsPhi(), seed);
            int gInv = LakeDestinationFinder.getInv(config.nrLakes(), g);
            ChunkPos pos = LakeDestinationFinder.teleportAim(
                        config,
                        new ChunkPos(x, z),
                        net.minecraft.util.math.random.Random.create(seed),
                        g,
                        gInv,
                        seed
                );
            assert LakeDestinationFinder.isSafeChunk(config, pos);
        }
    }

    @Test
    public void testTeleportAimNeverUnsafe() {
        Random random = new Random(42);
        testTeleportAimNeverUnsafeWithConfig(random, CONFIG);
        testTeleportAimNeverUnsafeWithConfig(random, MIDDLE_CONFIG);
        testTeleportAimNeverUnsafeWithConfig(random, smallPrimeConfig);
    }
}
