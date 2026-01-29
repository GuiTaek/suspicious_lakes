package com.gmail.guitaekm.enderlakes.lakes;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

import java.util.Optional;

public class FeatureStructure extends Structure {
    public static final MapCodec<FeatureStructure> CODEC = RecordCodecBuilder.mapCodec(
            codecBuilder -> codecBuilder.group(
                    Config.CODEC
                            .fieldOf("config")
                            .forGetter(elem -> elem.config),
                    ConfiguredFeature.CODEC
                            .fieldOf("configured_feature")
                            .forGetter(FeatureStructure::feature),
                    Heightmap.Type.CODEC
                            .fieldOf("heightmap")
                            .forGetter(FeatureStructure::heightmap),
                    Vec3i.CODEC
                            .fieldOf("dimensions")
                            .forGetter(FeatureStructure::dimensions),
                    Vec3i.CODEC
                            .fieldOf("anchor")
                            .forGetter(FeatureStructure::anchor)
            ).apply(codecBuilder, FeatureStructure::new)
    );

    final public ConfiguredFeature<? extends FeatureConfig, ? extends Feature<?>> feature;
    final public Heightmap.Type heightmap;
    final public Vec3i dimensions;
    final public Vec3i anchor;

    public ConfiguredFeature<? extends FeatureConfig, ? extends Feature<?>> feature() {
        return this.feature;
    }

    public Heightmap.Type heightmap() {
        return this.heightmap;
    }

    public Vec3i dimensions() {
        return this.dimensions;
    }

    public Vec3i anchor() {
        return this.anchor;
    }

    protected FeatureStructure(
            Config config,
            ConfiguredFeature<? extends FeatureConfig, ? extends Feature<?>> feature,
            Heightmap.Type heightmap,
            Vec3i dimensions,
            Vec3i anchor
    ) {
        super(config);
        this.feature = feature;
        this.heightmap = heightmap;
        this.dimensions = dimensions;
        this.anchor = anchor;
    }

    @Override
    protected Optional<StructurePosition> getStructurePosition(Context context) {
        ChunkPos pos = context.chunkPos();
        int x = pos.getCenterX();
        int z = pos.getCenterZ();
        int y = context.chunkGenerator().getHeight(x, z, this.heightmap, context.world(), context.noiseConfig());
            BlockPos blockPos = new BlockPos(x, y, z);
        StructurePosition generator = new StructurePosition(
                blockPos,
                structurePiecesCollector -> {
                    final Vec3i anchor = FeatureStructure.this.anchor;
                    Vec3i firstPosition = new Vec3i(x, y, z);
                    final Vec3i start = firstPosition.subtract(anchor);
                    final Vec3i end = start.add(FeatureStructure.this.dimensions);
                    BlockBox boundingBox = new BlockBox(
                            start.getX(), start.getY(), start.getZ(),
                            end.getX(), end.getY(), end.getZ()
                    );
                    structurePiecesCollector.addPiece(new FeatureStructurePiece(boundingBox, FeatureStructure.this));
                }
        );
        return Optional.of(generator);
    }

    @Override
    public StructureType<?> getType() {
        return Structures.FEATURE_STRUCTURE;
    }
}
