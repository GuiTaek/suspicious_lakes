package com.gmail.guitaekm.enderlakes.lakes;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.LakeFeature;
import net.minecraft.world.gen.feature.util.FeatureContext;

import static net.minecraft.block.Blocks.AIR;

@SuppressWarnings("deprecation")
public class ForcedLakeFeature extends Feature<LakeFeature.Config> {
    public ForcedLakeFeature(Codec<LakeFeature.Config> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean generate(FeatureContext<LakeFeature.Config> context) {
        BlockPos blockPos = context.getOrigin();
        StructureWorldAccess structureWorldAccess = context.getWorld();
        Random random = context.getRandom();
        LakeFeature.Config config = context.getConfig();
        blockPos = blockPos.down(4);
        boolean[] bls = new boolean[2048];
        int i = random.nextInt(4) + 4;

        for (int j = 0; j < i; ++j) {
            double d = random.nextDouble() * 6.0 + 3.0;
            double e = random.nextDouble() * 4.0 + 2.0;
            double f = random.nextDouble() * 6.0 + 3.0;
            double g = random.nextDouble() * (16.0 - d - 2.0) + 1.0 + d / 2.0;
            double h = random.nextDouble() * (8.0 - e - 4.0) + 2.0 + e / 2.0;
            double k = random.nextDouble() * (16.0 - f - 2.0) + 1.0 + f / 2.0;

            for (int l = 1; l < 15; ++l) {
                for (int m = 1; m < 15; ++m) {
                    for (int n = 1; n < 7; ++n) {
                        double o = ((double) l - g) / (d / 2.0);
                        double p = ((double) n - h) / (e / 2.0);
                        double q = ((double) m - k) / (f / 2.0);
                        double r = o * o + p * p + q * q;
                        if (r < 1.0) {
                            bls[(l * 16 + m) * 8 + n] = true;
                        }
                    }
                }
            }
        }

        BlockState blockState = config.fluid().get(random, blockPos);


        boolean bl2;
        for (int s = 0; s < 16; ++s) {
            for (int t = 0; t < 16; ++t) {
                for (int u = 0; u < 8; ++u) {
                    if (bls[(s * 16 + t) * 8 + u]) {
                        BlockPos blockPos2 = blockPos.add(s, u, t);
                        if (this.canReplace(structureWorldAccess.getBlockState(blockPos2))) {
                            bl2 = u >= 4;
                            structureWorldAccess.setBlockState(blockPos2, bl2 ? AIR.getDefaultState() : blockState, Block.NOTIFY_LISTENERS);
                        }
                    }
                }
            }
        }

        BlockState blockState3 = config.barrier().get(random, blockPos);
        for (int t = 0; t < 16; ++t) {
            for (int u = 0; u < 16; ++u) {
                for (int v = 0; v < 8; ++v) {
                    bl2 = !bls[(t * 16 + u) * 8 + v] && (t < 15 && bls[((t + 1) * 16 + u) * 8 + v] || t > 0 && bls[((t - 1) * 16 + u) * 8 + v] || u < 15 && bls[(t * 16 + u + 1) * 8 + v] || u > 0 && bls[(t * 16 + (u - 1)) * 8 + v] || v < 7 && bls[(t * 16 + u) * 8 + v + 1] || v > 0 && bls[(t * 16 + u) * 8 + (v - 1)]);
                    if (bl2 && (v < 4 || random.nextInt(2) != 0)) {
                        BlockPos blockPos3 = blockPos.add(t, v, u);
                        structureWorldAccess.setBlockState(blockPos3, blockState3, Block.NOTIFY_LISTENERS);
                    }
                }
            }
        }

        if (blockState.getFluidState().isIn(FluidTags.WATER)) {
            for (int t = 0; t < 16; ++t) {
                for (int u = 0; u < 16; ++u) {
                    BlockPos blockPos4 = blockPos.add(t, 4, u);
                    if (structureWorldAccess.getBiome(blockPos4).value().canSetIce(structureWorldAccess, blockPos4, false) && this.canReplace(structureWorldAccess.getBlockState(blockPos4))) {
                        structureWorldAccess.setBlockState(blockPos4, Blocks.ICE.getDefaultState(), Block.NOTIFY_LISTENERS);
                    }
                }
            }
        }

        return true;
    }

    private boolean canReplace(BlockState state) {
        return !state.isIn(BlockTags.FEATURES_CANNOT_REPLACE);
    }
}
