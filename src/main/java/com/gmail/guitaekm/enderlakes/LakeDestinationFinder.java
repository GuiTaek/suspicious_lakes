package com.gmail.guitaekm.enderlakes;

import com.google.common.collect.Streams;
import net.minecraft.util.math.random.LocalRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Follows math.md to implement the logic in this mod
 */
@SuppressWarnings("SuspiciousNameCombination")
public class LakeDestinationFinder {
    public static int pi(int i, ConfigInstance config) {
        int weightSum = config.cycleWeights().stream().mapToInt(w -> w).sum();
        int[] nrLakesPerCycle = config.cycleWeights()
                .stream()
                .mapToDouble(w -> (double) w / weightSum)
                .mapToInt(p -> (int) Math.round(config.nrLakes() * p))
                .toArray();
        int unUsedCycles = i;
        for (int cycleLength = 1; cycleLength <= nrLakesPerCycle.length; cycleLength++) {
            unUsedCycles -= nrLakesPerCycle[cycleLength - 1];
            if (unUsedCycles < 0) {
                int temp = unUsedCycles % cycleLength;
                int cyclePos;
                if (temp < 0) {
                    cyclePos = temp + cycleLength;
                } else {
                    cyclePos = temp;
                }
                if (cyclePos == cycleLength - 1) {
                    return i - cycleLength + 1;
                }
                return i + 1;
            }
        }
        if (i < config.nrLakes()) {
            return i;
        }
        throw new IllegalArgumentException("integer not part of the permutation");
    }

    // it is difficult to include minecraft's ChunkPos into the test module, I wasn't able to
    /*
    public record ChunkPos(int x, int z) {
        public net.minecraft.util.math.ChunkPos toMinecraft() {
            return new net.minecraft.util.math.ChunkPos(x, z);
        }
        public ChunkPos(net.minecraft.util.math.ChunkPos pos) {
            this(pos.x, pos.z);
        }
    }

     */

    public record GridPos(int x, int y) { }

    public static GridPos c(int i) {
        if (i <= 0) {
            throw new IllegalArgumentException("give positive input to c");
        }
        int i2 = i - 1;
        // the ordering isn't relevant, because of g therefore no beautiful bijection

        // x can't be 0 but y can, then rotate to get every grid point except (0, 0)
        int rotate = i2 % 4;
        int i3 = i2 / 4;

        // calculate s := x + y by solving for s(s + 1) / 2 = n = i3 + 1 and rounding up (mathematical proof omitted)
        double sFloat = -0.5d + Math.sqrt(1.25d + 2d * (double)i3);
        // if the ceil method would return 0.99999999999999999999999999999998 then (int) would return 0, which is not
        // want. I'm not sure, if this is really the case, and it isn't worth it looking it up.
        int s = (int) (Math.ceil(sFloat) + 0.1d);
        int y = s * (s + 1) / 2 - i3 - 1;

        int x = s - y;

        return switch (rotate) {
            case 0 -> new GridPos(+x, +y);
            case 1 -> new GridPos(-y, +x);
            case 2 -> new GridPos(-x, -y);
            case 3 -> new GridPos(+y, -x);
            default -> throw new IllegalStateException("Rotating should be one of 0, 1, 2, 3. Inform the developer of this mod.");
        };
    }

    public static int getRotation(int x, int y) {
        // remember y can be 0
        assert x != 0 || y != 0;
        if (x == 0) {
            return y > 0 ? 1 : 3;
        }
        if (y == 0) {
            return x > 0 ? 0 : 2;
        }
        if (x > 0) {
            if (y > 0) {
                return 0;
            }
            return 3;
        }
        if (y > 0) {
            return 1;
        }
        return 2;
    }

    public static int cInv(int x, int y) {
        if (x == 0 && y == 0) {
            throw new IllegalArgumentException("May not contain the origin");
        }

        int rotation = getRotation(x, y);
        x = Math.abs(x);
        y = Math.abs(y);
        switch(rotation) {
            case 1, 3:
                int temp = x;
                x = y;
                y = temp;
        }
        int s = x + y;
        int res = (s + 1) * s / 2 - y - 1;
        return res * 4 + rotation + 1;
    }
    public static int cInv(GridPos output) {
        return cInv(output.x, output.y);
    }
    public static int f(ConfigInstance config, int c) {
        int signum = Integer.compare(c, 0);
        return (int) (signum * config.minimumDistance() * Math.round(Math.pow(Math.abs(c), config.powerDistance())));
    }
    public static int fInv(ConfigInstance config, int c) {
        int signum = Integer.compare(c, 0);
        return signum * (int) (Math.round(Math.pow((double) Math.abs(c) / config.minimumDistance(), 1d / config.powerDistance())));
    }

    /**
     * in math.md, it's rawPos
     * @param config a config instance e.g. self defined through ConfigInstance or defined through the player
     * @param x grid x coordinate
     * @param y grid y coordinate
     * @return the chunk position that the grid position relates to in the minecraft world
     */
    public static ChunkPos rawPos(ConfigInstance config, int x, int y) {
        ChunkPos res = new ChunkPos(f(config, x), f(config, y));
        if (-64 <= res.x && res.x <= +64
                && -64 <= res.z && res.z <= +64) {
            throw new IllegalArgumentException("rawPos got a pos in the 128x128 chunks of the start island");
        }
        return res;
    }

    /**
     * in math.md, it's rawPos
     * @param config a config instance e.g. self defined through ConfigInstance or defined through the player
     * @param gridCoords the coordinates in the grid not really being chunk coordinates
     * @return the chunk position that the grid position relates to in the minecraft world
     */
    public static ChunkPos rawPos(ConfigInstance config, GridPos gridCoords) {
        return rawPos(config, gridCoords.x, gridCoords.y);
    }

    /**
     * in math.md it's pos + off
     * @param config a config instance e.g. self defined through ConfigInstance or defined through the player
     * @param seed the seed of the world or the random generator if not used in a minecraft setting
     * @param x the grid x position
     * @param y the grid y position
     * @return the chunk where the lake should ly in
     */
    public static ChunkPos pos(ConfigInstance config, long seed, int x, int y) {
        if (x == 0 && y == 0) {
            throw new IllegalArgumentException("off shall not get the origin");
        }
        Random random = Random.create(seed ^ ((long) x * config.nrLakes() + y));
        int offX, offZ;
        {
            ChunkPos fromPos;
            try {
                fromPos = rawPos(config, x, y - 1);
                rawPos(config, x - 1, y - 1);
                rawPos(config, x + 1, y - 1);
            } catch(IllegalArgumentException exc) {
                fromPos = rawPos(config, x, y);
            }
            ChunkPos toPos;
            try {
                toPos = rawPos(config, x, y + 1);
                rawPos(config, x - 1, y + 1);
                rawPos(config, x + 1, y + 1);
            } catch(IllegalArgumentException exc) {
                toPos = rawPos(config, x, y);
            }
            offZ = random.nextBetween(fromPos.z, toPos.z);
        }
        {
            ChunkPos fromPos;
            try {
                fromPos = rawPos(config, x - 1, y);
                rawPos(config, x - 1, y - 1);
                rawPos(config, x - 1, y + 1);
            } catch(IllegalArgumentException exc) {
                fromPos = rawPos(config, x, y);
            }
            ChunkPos toPos;
            try {
                toPos = rawPos(config, x + 1, y);
                rawPos(config, x + 1, y - 1);
                rawPos(config, x + 1, y + 1);
            } catch(IllegalArgumentException exc) {
                toPos = rawPos(config, x, y);
            }
            offX = random.nextBetween(fromPos.x, toPos.x);
        }
        return new ChunkPos(offX, offZ);
    }

    /**
     *
     * @param config a config instance e.g. self defined through ConfigInstance or defined through the player
     * @param seed the seed of the world or the random generator if not used in a minecraft setting
     * @param pos the position in the grid
     * @return the chunk where the lake should ly in
     */
    public static ChunkPos pos(ConfigInstance config, long seed, GridPos pos) {
        return pos(config, seed, pos.x, pos.y);
    }

    public static GridPos getRawGridPos(ConfigInstance config, ChunkPos pos) {
        return new GridPos(fInv(config, pos.x), fInv(config, pos.z));
    }

    public static Set<GridPos> findNearestLake(ConfigInstance config, long seed, ChunkPos pos) {
        GridPos basePos = getRawGridPos(config, pos);
        int nearestDistanceSquared = Integer.MAX_VALUE;
        HashSet<GridPos> nearestLake = new HashSet<>();
        // the offset of pos relative to rawPos is inside a 2x2 GridPos-Rectangle, therefore -2 to +2
        for (int xDiff = -2; xDiff <= +2; xDiff++) {
            for (int yDiff = -2; yDiff <= +2; yDiff++) {
                GridPos currGridPos = new GridPos(basePos.x + xDiff, basePos.y + yDiff);
                try {
                    rawPos(config, currGridPos);
                } catch (IllegalArgumentException exc) {
                    continue;
                }
                int currDistanceSquared = pos(config, seed, currGridPos)
                        .getSquaredDistance(pos);
                if (currDistanceSquared == nearestDistanceSquared) {
                    nearestLake.add(currGridPos);
                }
                else if (currDistanceSquared < nearestDistanceSquared) {
                    nearestDistanceSquared = currDistanceSquared;
                    nearestLake = new HashSet<>();
                    nearestLake.add(currGridPos);
                }
            }
        }
        return nearestLake;
    }

    public static boolean isPrimitiveRootFast(int g, int n, int[] factsPhi) {
        // n is asserted to be a prime number
        int phiN = n - 1;
        for (int fact : factsPhi) {
            if (modularExponentiationBySquaring(g, phiN / fact, n) == 1) {
                return false;
            }
        }
        return true;
    }


    public static boolean isPrimitiveRootSlow(int g, int n) {
        assert n <= 10_000_000;
        assert isPrime(n);
        // n is asserted to be a prime number
        int phiN = n - 1;
        // primeFactors(phiN) could be a constant saved in the code
        int[] factsPhi = primeFactors(phiN).stream().mapToInt(i -> i).toArray();
        return isPrimitiveRootFast(g, n, factsPhi);

    }

    /** implements <a href="https://en.wikipedia.org/wiki/Modular_exponentiation#Pseudocode">modular exponentation</a>
     * but with lambdas so you can use it for multiplication
     *
     * @return result of multiplication/exponentation
     */
    private static int modularMultExpHelper(
            int b,
            int e,
            int n,
            Function<Integer, Long> squareOperation,
            BiFunction<Integer, Integer, Long> multiplyOperation,
            int neutralElement
    ) {
        if (n == 1) {
            return 0;
        }
        int result = neutralElement;
        b = b % n;
        while (e > 0) {
            if (e % 2 == 1) {
                // conversion allowed because of % n and n is int
                result = (int)((multiplyOperation.apply(result, b) % n + n) % n);
            }
            e = e >> 1;
            // conversion allowed because of % n and n is int
            b = (int)((squareOperation.apply(b) % n + n) % n);
        }
        return (result % n + n) % n;
    }

    /**
     * fast modular Multiplication. Inspired by <a href="https://stackoverflow.com/a/20677244/3289974">this post</a>
     * also keep my comment in mind
     * @param a a factor
     * @param b a factor
     * @param N the divisor
     * @return (a * b) % N
     */
    public static int modularMultiplicationByDoubling(int a, int b, int N) {
        return modularMultExpHelper(
                a, b, N,
                b_ -> 2 * (long)b_,
                (a_, b_) -> (long)a_ + (long)b_,
                0
        );
    }

    public static int modularExponentiationBySquaring(int b, int e, int N) {
        return modularMultExpHelper(
                b, e, N,
                b_ -> (long)b_ * (long)b_,
                (a_, b_) -> (long)a_ * (long)b_,
                1
        );
    }

    // not used
    public static int phi(int num) {
        ArrayList<Integer> raw_factors = primeFactors(num);
        ArrayList<Integer> unique_factors = new ArrayList<>();
        ArrayList<Integer> pow = new ArrayList<>();
        // there are usually not that many factors in there, therefore lists may be used
        for (Integer fact : raw_factors) {
            if (!unique_factors.contains(fact)) {
                unique_factors.add(fact);
                pow.add(0);
            }
        }
        for (Integer fact : raw_factors) {
            int ind = unique_factors.indexOf(fact);
            pow.set(ind, pow.get(ind) + 1);
        }
        return Streams.zip(
                unique_factors.stream(),
                pow.stream(),
                (fact, pow1) -> Math.toIntExact(
                        Math.round(Math.pow(fact, pow1) - Math.pow(fact, pow1 - 1))
                )
        ).reduce(1, (a, b) -> a*b);
    }

    // from https://stackoverflow.com/a/6233030/3289974
    public static ArrayList<Integer> primeFactors(int num)
    {
        assert num > 1;
        assert num < 300_000_000;
        ArrayList<Integer> factors = new ArrayList<>();
        for (int a = 2;  num>1; ) {
            if (num%a==0) {
                factors.add(a);
                num/=a;
            }
            else {
                a++;
            }
        }
        Collections.sort(factors);
        return factors;
    }

    public static boolean isPrime(int n) {
        if (n < 2) {
            return false;
        }
        return primeFactors(n).size() == 1;
    }

    public static GridPos teleportAim(
            ConfigInstance config,
            GridPos oldPos,
            int g,
            int gInv
    ) {
        assert modularMultiplicationByDoubling(g, gInv, config.nrLakes()) == 1;
        int number = modularMultiplicationByDoubling(g, cInv(oldPos), config.nrLakes());
        int mappedNumber = modularMultiplicationByDoubling(gInv, pi(number, config), config.nrLakes());
        return c(mappedNumber);
    }
    public static ChunkPos teleportAim(
                ConfigInstance config,
                ChunkPos oldPos,
                Random minecraftRandom,
                int g,
                int gInv,
                long seed
    ) {
        assert isPrimitiveRootFast(g, config.nrLakes(), config.factsPhi());
        Set<GridPos> gridPositions = LakeDestinationFinder.findNearestLake(config, seed, oldPos);
        GridPos gridPos = new ArrayList<>(gridPositions).get(
                (int)(gridPositions.size() * minecraftRandom.nextDouble())
        );
        return pos(config, seed, teleportAim(config, gridPos, g, gInv));
    }

    public static int getG(int N, int[] factsPhi, long seed) {
        Random random = new LocalRandom(seed);
        int g = random.nextBetween(1, N - 1);
        while (isPrimitiveRootFast(g, N, factsPhi)) {
            g = random.nextBetween(1, N - 1);
        }
        return g;
    }

    public static int getInv(int N, int g) {
        int a = g;
        int b = N;
        // extended euclidean algorithm from https://en.wikipedia.org/wiki/Extended_Euclidean_algorithm
        int oldR = a, r = b;
        int oldS = 1, s = 0;
        int oldT = 0, t = 1;
        while (r != 0) {
            int quotient = oldR / r;

            int tempR = r;
            r = oldR - quotient * r;
            oldR = tempR;

            int tempS = s;
            s = oldS - quotient * s;
            oldS = tempS;

            int tempT = t;
            t = oldT - quotient * t;
            oldT = tempT;
        }
        // oldS can be negative
        return (oldS + N) % N;
    }
}
