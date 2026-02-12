package com.gmail.guitaekm.enderlakes;

import com.google.common.collect.Streams;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.LocalRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.math.BigInteger;
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

    public record GridPos(int x, int y) { }

    public static GridPos c(int i) {
        if (i <= 0) {
            throw new IllegalArgumentException("give positive input to c");
        }
        int i2 = i - 1;
        // the ordering isn't relevant, because of g therefore no beautiful bijection

        // x can't be 0 but y can, then rotate to get every grid point except (0, 0)

        // this is too early for rotate
        //int rotate = i2 % 4;
        //int i3 = i2 / 4;
        int i3 = i2;

        // calculate rotation rot, radius r and angle aby solving for 8 + 16 + 24 + ... + 8(r - 1) + 2r * rot + a = i3
        // where a is between 0 and 2r - 1
        double rDouble = (4d + Math.sqrt(16d + 16d * i3)) / 8d;

        // if the ceil method would return 0.99999999999999999999999999999998 then (int) would return 0, which is not
        // want. I'm not sure, if this is really the case, and it isn't worth it looking it up.
        int radius = (int) (Math.floor(rDouble) + 0.1);
        int rotate = i3 / 2 / radius - 2 * (radius - 1);
        int angle = i3 - 4 * (radius - 1) * radius - rotate * 2 * radius;

        int x = radius;
        int y = angle - radius + 1;
        GridPos rawPos = new GridPos(x, y);
        return Matrix2d.ROTATIONS.get(rotate).multiply(rawPos);
    }

    public static int getRotation(GridPos pos) {
        return getRotation(pos.x(), pos.y());
    }

    public static int getRotation(int x, int y) {
        // remember y can be 0
        assert x != 0 || y != 0;

        // x > 0, Math.abs(y) < +x: 0
        // y > 0, Math.abs(x) < +y: 1
        // x < 0, Math.abs(y) < -x: 2
        // y < 0, Math.abs(x) < -y: 3

        if (Math.abs(x) < Math.abs(y)) {
            return y > 0 ? 1 : 3;
        }

        if (Math.abs(x) > Math.abs(y)) {
            return x > 0 ? 0 : 2;
        }

        // x != 0 && y != 0
        // Math.abs(x) == Math.abs(y)
        // ++: 0
        // -+: 1
        // --: 2
        // +-: 3
        if (x > 0) {
            return y > 0 ? 0 : 3;
        } else {
            return y > 0 ? 1 : 2;
        }
    }

    public static int cInv(GridPos pos) {
        if (pos.equals(new GridPos(0, 0))) {
            throw new IllegalArgumentException("May not use origin in cInv. Contact the developer of this mod.");
        }
        int rotation = getRotation(pos);
        GridPos normalizedPos = Matrix2d.ROTATIONS.get(rotation).inv().multiply(pos);

        int radius = normalizedPos.x();

        // between 0 and 2 * radius - 1
        int angle = normalizedPos.y() + radius - 1;

        return 4 * (radius - 1) * radius + rotation * 2 * radius + angle + 1;
    }

    public static int cInv(int x, int y) {
        return cInv(new GridPos(x, y));
    }

    public static int f(ConfigInstance config, int c) {
        int signum = Integer.compare(c, 0);
        return (int) (signum * config.minimumDistance() * Math.round(Math.pow(Math.abs(c), config.powerDistance())));
    }

    public static double fInvRaw(ConfigInstance config, int c) {
        int signum = Integer.compare(c, 0);
        return signum * Math.pow((double) Math.abs(c) / config.minimumDistance(), 1d / config.powerDistance());
    }

    public static int fInv(ConfigInstance config, int c) {
        return (int) Math.round(fInvRaw(config, c));
    }

    public static int fInvFloor(ConfigInstance config, int c) {
        int signum = Integer.compare(c, 0);
        return signum * (int) Math.floor(signum * fInvRaw(config, c));
    }

    public static int fInvCeil(ConfigInstance config, int c) {
        int signum = Integer.compare(c, 0);
        return signum * (int) Math.ceil(signum * fInvRaw(config, c));
    }

    /**
     * generates a prime needed for this whole math depending on the world border
     * @param config the config for which the prime number will be generated. nrLakes and factsPhi will be ignored
     * @param border the distance in x or z direction from the border to spawn
     * @param signum the direction the prime is searched in (positive or negative). Must be -1 or 1. If positive,
     *               there might be some lake cycles on the map that have one or unlikely more than one lake removed.
     *               Can lead to lakes pointing on themselves. If negative, the last lakes of the map may be one-directional
     * @return the new prime
     */
    public static int findNewNrLakes(ConfigInstance config, int border, int signum) {
        assert Math.abs(signum) == 1;
        // this is the position of the last raw Pos, therefore approximately the last lake
        ChunkPos maxChunk = lastPos(config, new ChunkPos(new BlockPos(border / 2, 0, border / 2)));

        GridPos gridPos = signum == -1 ? new GridPos(fInvFloor(config, maxChunk.x), fInvFloor(config, maxChunk.z))
                : new GridPos(fInvCeil(config, maxChunk.x), fInvCeil(config, maxChunk.z));
        BigInteger prime = BigInteger.valueOf(cInv(gridPos));
        while (!prime.isProbablePrime(1_000)) {
            prime = prime.add(BigInteger.valueOf(signum));
        }

        // the primes don't get big here, it's just the method that requires a BigInteger
        return prime.intValue();
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
        if (!isSafeChunk(config, res)) {
            throw new IllegalArgumentException("rawPos got a non safe pos: %s".formatted(res.toString()));
        }
        return res;
    }

    public static ChunkPos rawPosUnsafe(ConfigInstance config, int x, int y) {
        ChunkPos res = new ChunkPos(f(config, x), f(config, y));
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
     * in maths.md it's pos + off
     * @param config a config instance e.g. self defined through ConfigInstance or defined through the player
     * @param seed the seed of the world or the random generator if not used in a minecraft setting
     * @param x the grid x position
     * @param y the grid y position
     * @return the chunk where the lake should lie in
     */
    public static ChunkPos pos(ConfigInstance config, long seed, int x, int y) {
        if (x == 0 && y == 0) {
            throw new IllegalArgumentException("off shall not get the origin");
        }
        Random random = Random.create(seed ^ cInv(new GridPos(x, y)));
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

    public static boolean isSafeChunk(ConfigInstance config, ChunkPos pos) {
        return Math.abs(pos.x) > config.lastUnsafeChunk() || Math.abs(pos.z) > config.lastUnsafeChunk();
    }

    public static GridPos getRawGridPos(ConfigInstance config, ChunkPos pos) {
        if (!LakeDestinationFinder.isSafeChunk(config, pos)) {
            if (Math.abs(pos.x) > Math.abs(pos.z)) {
                int signum = Integer.compare(pos.x, 0);
                pos = new ChunkPos(config.lastUnsafeChunk() * signum, pos.z);
            } else {
                int signum = Integer.compare(pos.z, 0);
                pos = new ChunkPos(pos.x, config.lastUnsafeChunk() * signum);
            }
        }
        return getRawGridPosUnsafe(config, pos);
    }

    private static GridPos getRawGridPosUnsafe(ConfigInstance config, ChunkPos pos) {
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
                // todo: better test for unsafe Chunk instead of catching an error
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

    /** implements <a href="https://en.wikipedia.org/wiki/Modular_exponentiation#Pseudocode">modular exponentiation</a>
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
        assert n > 1;
        int result = neutralElement;
        b = b % n;
        while (e > 0) {
            if (e % 2 == 1) {
                result = mod(multiplyOperation.apply(result, b), n);
            }
            e = e >> 1;
            b = mod(squareOperation.apply(b), n);
        }
        return mod(result, n);
    }

    private static int mod(long a, int N) {
        // conversion allowed because of % N and N is int
        return (int)((a % N + N) % N);
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
        // todo: is this function wrong? shouldn't it be num - 1?
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
        for (int a = 2;  num > 1; ) {
            boolean changed = false;
            while (num % a == 0) {
                factors.add(a);
                num /= a;
                changed = true;
            }
            if (changed && BigInteger.valueOf(num).isProbablePrime(200)) {
                factors.add(num);
                break;
            }
            if (!changed) {
                a++;
            }
        }
        Collections.sort(factors);
        return factors;
    }

    // todo: this has to be removed, because there is a faster method and this method shouldn't be used
    public static boolean isPrime(int n) {
        if (n < 2) {
            return false;
        }
        return primeFactors(n).size() == 1;
    }

    public static int lastUnsafeInteger(ConfigInstance config) {
        int coord = config.lastUnsafeChunk();
        ChunkPos pos = lastPos(config, new ChunkPos(coord, coord));
        return cInv(getRawGridPosUnsafe(config, pos));
    }

    public static ChunkPos lastPos(ConfigInstance config, ChunkPos unRotated) {
        assert Math.abs(unRotated.x) == Math.abs(unRotated.z);
        return new ChunkPos(Math.abs(unRotated.x), -Math.abs(unRotated.z));
    }

    public static GridPos teleportAim(
            ConfigInstance config,
            GridPos oldPos,
            int g,
            int gInv
    ) {
        assert modularMultiplicationByDoubling(g, gInv, config.nrLakes()) == 1;
        int o = lastUnsafeInteger(config);
        int number = modularMultiplicationByDoubling(g, cInv(oldPos) - o, config.nrLakes());
        int mappedNumber = modularMultiplicationByDoubling(gInv, pi(number, config), config.nrLakes()) + o;
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

    public static ChunkPos safeTeleportAim(
            World world,
            ConfigInstance config,
            ChunkPos oldPos,
            Random minecraftRandom,
            int g,
            int gInv,
            long seed
    ) {
        ChunkPos currPos = oldPos;
        int counter = 0;
        do {
            currPos = teleportAim(config, currPos, minecraftRandom, g, gInv, seed);
            if (counter > config.cycleWeights().size()) {
                // some fail-safe if someone enters a lake outside the border and none of the cycle is within
                return null;
            }
            counter++;
        } while (!world.getWorldBorder().contains(currPos.getBlockPos(8, 0, 8)));
        return currPos;
    }

    private static int g;
    private static Long lastSeed = null;

    public static int getG(int N, int[] factsPhi, long seed) {
        if (!Objects.equals(seed, lastSeed)) {
            LakeDestinationFinder.g = calculateG(N, factsPhi, seed);
            LakeDestinationFinder.lastSeed = seed;
        }
        return LakeDestinationFinder.g;
    }

    public static int calculateG(int N, int[] factsPhi, long seed) {
        Random random = new LocalRandom(seed);
        do {
            g = random.nextBetween(1, N - 1);
        } while (!isPrimitiveRootFast(g, N, factsPhi));
        return g;
    }

    public static int calculateInv(int N, int g) {
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

    private static Integer lastG = null;
    private static int gInv;

    public static int getInv(int N, int g) {
        if (!Objects.equals(g, lastG)) {
            gInv = calculateInv(N, g);
            lastG = g;
        }
        return LakeDestinationFinder.gInv;
    }
}
