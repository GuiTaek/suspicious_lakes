package com.gmail.guitaekm.enderlakes.lakes;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiece;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.LakeFeature;

public class FeatureStructurePiece extends StructurePiece {
    final public static MapCodec<FeatureStructurePiece> CODEC = RecordCodecBuilder.mapCodec(
            codecBuilder -> codecBuilder.group(
                    BlockBox.CODEC.fieldOf("bounding_box").forGetter(FeatureStructurePiece::getBoundingBox),
                    FeatureStructure.CODEC.fieldOf("feature_structure").forGetter(FeatureStructurePiece::structure)
            ).apply(codecBuilder, FeatureStructurePiece::new)
    );
    final public FeatureStructure structure;
    public FeatureStructure structure() {
        return this.structure;
    }
    protected FeatureStructurePiece(BlockBox boundingBox, FeatureStructure structure) {
        super(Structures.FEATURE_PIECE, 1, boundingBox);
        this.structure = structure;
    }

    protected FeatureStructurePiece(StructureContext ignoredContext, NbtCompound nbt) {
        super(Structures.FEATURE_PIECE, nbt);
        this.structure = FeatureStructure.CODEC
                .codec()
                .decode(NbtOps.INSTANCE, nbt)
                .getOrThrow()
                .getFirst();
    }

    @Override
    protected void writeNbt(StructureContext context, NbtCompound nbt) {
        FeatureStructure.CODEC.codec().encode(this.structure, NbtOps.INSTANCE, nbt);
    }

    @Override
    public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        BlockPos start = new BlockPos(
                boundingBox.getMinX(),
                boundingBox.getMinY(),
                boundingBox.getMinZ()
        );
        this.structure.feature.generate(world, chunkGenerator, random, start);
    }
}
