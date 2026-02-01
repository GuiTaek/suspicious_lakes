package com.gmail.guitaekm.enderlakes.lakes;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructureLiquidSettings;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolBasedGenerator;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.alias.StructurePoolAliasLookup;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SuspiciousLakeStructure extends Structure {
    public static final MapCodec<SuspiciousLakeStructure> CODEC = RecordCodecBuilder.mapCodec(
            codecBuilder -> codecBuilder.group(
                    Config.CODEC.fieldOf("config").forGetter(SuspiciousLakeStructure::config),
                    StructurePool.REGISTRY_CODEC.fieldOf("start_pool").forGetter(SuspiciousLakeStructure::startPool),
                    Codecs.POSITIVE_INT.fieldOf("depth").forGetter(SuspiciousLakeStructure::depth),
                    Heightmap.Type.CODEC.fieldOf("heightmap").forGetter(SuspiciousLakeStructure::heightmap),
                    Vec3i.CODEC.optionalFieldOf("offset", Vec3i.ZERO).forGetter(SuspiciousLakeStructure::offset)
            ).apply(codecBuilder, SuspiciousLakeStructure::new)
    );
    public final int depth;
    public final Heightmap.Type heightmap;
    public final RegistryEntry<StructurePool> startPool;
    public final Vec3i offset;

    protected SuspiciousLakeStructure(
            Config config,
            RegistryEntry<StructurePool> startPool,
            int depth,
            Heightmap.Type heightmap,
            Vec3i offset
    ) {
        super(config);
        this.depth = depth;
        this.heightmap = heightmap;
        this.startPool = startPool;
        this.offset = offset;
    }

    public Config config() {
        return this.config;
    }

    public RegistryEntry<StructurePool> startPool() {
        return this.startPool;
    }

    public int depth() {
        return depth;
    }

    public Heightmap.Type heightmap() {
        return this.heightmap;
    }

    public Vec3i offset() {
        return offset;
    }

    @Override
    protected Optional<StructurePosition> getStructurePosition(Context context) {
        ChunkPos pos = context.chunkPos();
        int surface = context.chunkGenerator().getHeight(
                pos.getCenterX(),
                pos.getCenterZ(),
                Heightmap.Type.WORLD_SURFACE_WG,
                context.world(),
                context.noiseConfig()
        );
        int y;
        if (surface - depth <= context.world().getBottomY()) {
            if (surface > context.world().getBottomY()) {
                y = 1;
            } else {
                y = context.random().nextBetween(context.world().getBottomY() + 2, context.world().getTopY() - 10);
            }
        } else {
            y = surface - this.depth;
        }
        // this is to make sure the player never falls to death, the coordinates where the player is supposed to land on are 8, 8
        BlockPos blockPos = pos.getBlockPos(0, y, 0);
        blockPos = blockPos.add(offset);

        // starting from here, it's from TelepathicGrunt's Structure Tutorial
        // Optional thing to control whether the structure will be waterlogged when replacing pre-existing water in the world.

        /*
         * Note, you are always free to make your own StructurePoolBasedGenerator class and implementation of how the structure
         * should generate. It is tricky but extremely powerful if you are doing something that vanilla's jigsaw system cannot do.
         * Such as for example, forcing 3 pieces to always spawn every time, limiting how often a piece spawns, or remove the intersection limitation of pieces.
         */

        // Return the pieces generator that is now set up so that the game runs it when it needs to create the layout of structure pieces.
        return Generator.generate(
                context,
                this.startPool,
                1,
                blockPos,
                StructurePoolAliasLookup.EMPTY,
                StructureLiquidSettings.IGNORE_WATERLOGGING
        );
    }

    @Override
    public StructureType<?> getType() {
        return Structures.SUSPICIOUS_LAKE_STRUCTURE;
    }

    public static class Generator {
        public static Optional<StructurePosition> generate(
                Context context,
                RegistryEntry<StructurePool> structurePool,
                int size,
                BlockPos pos,
                StructurePoolAliasLookup aliasLookup,
                StructureLiquidSettings liquidSettings
        ) {
            DynamicRegistryManager dynamicRegistryManager = context.dynamicRegistryManager();
            ChunkGenerator chunkGenerator = context.chunkGenerator();
            StructureTemplateManager structureTemplateManager = context.structureTemplateManager();
            HeightLimitView heightLimitView = context.world();
            ChunkRandom chunkRandom = context.random();
            Registry<StructurePool> registry = dynamicRegistryManager.get(RegistryKeys.TEMPLATE_POOL);
            BlockRotation blockRotation = BlockRotation.random(chunkRandom);
            StructurePool structurePoolWithDefault = structurePool.getKey()
                    .flatMap(
                            (key) -> registry.getOrEmpty(aliasLookup.lookup(key)))
                    .orElse(structurePool.value());
            StructurePoolElement structurePoolElement = structurePoolWithDefault.getRandomElement(chunkRandom);
            BlockBox tempBoundingBox = structurePoolElement
                    .getBoundingBox(structureTemplateManager, new BlockPos(0, 0, 0), blockRotation);
            Vec3i structureOffset = tempBoundingBox.getCenter().withY(0);
            BlockPos blockPos = pos.subtract(structureOffset);
            PoolStructurePiece poolStructurePiece = new PoolStructurePiece(structureTemplateManager, structurePoolElement, blockPos, structurePoolElement.getGroundLevelDelta(), blockRotation, structurePoolElement.getBoundingBox(structureTemplateManager, blockPos, blockRotation), liquidSettings);
            BlockBox blockBox = poolStructurePiece.getBoundingBox();
            return Optional.of(new StructurePosition(blockPos, (collector) -> {
                List<PoolStructurePiece> list = Lists.newArrayList();
                list.add(poolStructurePiece);
                if (size > 0) {
                    Box box = new Box(
                            blockBox.getMinX(), blockBox.getMinY(), blockBox.getMinZ(),
                            blockBox.getMaxX(), blockBox.getMaxY(), blockBox.getMaxZ()
                            );
                    VoxelShape voxelShape = VoxelShapes.combineAndSimplify(VoxelShapes.cuboid(box), VoxelShapes.cuboid(Box.from(blockBox)), BooleanBiFunction.ONLY_FIRST);

                    StructurePoolBasedGenerator.generate(context.noiseConfig(), size, true, chunkGenerator, structureTemplateManager, heightLimitView, chunkRandom, registry, poolStructurePiece, list, voxelShape, aliasLookup, liquidSettings);

                    Objects.requireNonNull(collector);
                    list.forEach(collector::addPiece);
                }
            }));
        }
    }
}
