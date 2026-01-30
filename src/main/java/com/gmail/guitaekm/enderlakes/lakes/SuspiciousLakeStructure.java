package com.gmail.guitaekm.enderlakes.lakes;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.StructureLiquidSettings;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolBasedGenerator;
import net.minecraft.structure.pool.alias.StructurePoolAliasLookup;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.structure.DimensionPadding;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

import java.util.Optional;

public class SuspiciousLakeStructure extends Structure {
    public static final MapCodec<SuspiciousLakeStructure> CODEC = RecordCodecBuilder.mapCodec(
            codecBuilder -> codecBuilder.group(
                    Config.CODEC.fieldOf("config").forGetter(SuspiciousLakeStructure::config),
                    StructurePool.REGISTRY_CODEC.fieldOf("start_pool").forGetter(SuspiciousLakeStructure::startPool),
                    Codecs.POSITIVE_INT.fieldOf("depth").forGetter(SuspiciousLakeStructure::depth),
                    Heightmap.Type.CODEC.fieldOf("heightmap").forGetter(SuspiciousLakeStructure::heightmap)
            ).apply(codecBuilder, SuspiciousLakeStructure::new)
    );
    public final int depth;
    public final Heightmap.Type heightmap;
    RegistryEntry<StructurePool> startPool;

    protected SuspiciousLakeStructure(Config config, RegistryEntry<StructurePool> startPool, int depth, Heightmap.Type heightmap) {
        super(config);
        this.depth = depth;
        this.heightmap = heightmap;
        this.startPool = startPool;
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
        int y = Math.max(context.world().getBottomY() + 1, surface - this.depth);
        // this is to make sure the player never falls to death, the coordinates where the player is supposed to land on are 8, 8
        BlockPos blockPos = pos.getBlockPos(11, y, 5);

        // starting from here, it's from TelepathicGrunt's Structure Tutorial
        // Optional thing to control whether the structure will be waterlogged when replacing pre-existing water in the world.

        /*
         * Note, you are always free to make your own StructurePoolBasedGenerator class and implementation of how the structure
         * should generate. It is tricky but extremely powerful if you are doing something that vanilla's jigsaw system cannot do.
         * Such as for example, forcing 3 pieces to always spawn every time, limiting how often a piece spawns, or remove the intersection limitation of pieces.
         */

        // Return the pieces generator that is now set up so that the game runs it when it needs to create the layout of structure pieces.
        return StructurePoolBasedGenerator.generate(
                context, // Used for StructurePoolBasedGenerator to get all the proper behaviors done.
                this.startPool, // The starting pool to use to create the structure layout from
                Optional.empty(), // Can be used to only spawn from one Jigsaw block. But we don't need to worry about this.
                1, // How deep a branch of pieces can go away from center piece. (5 means branches cannot be longer than 5 pieces from center piece)
                blockPos, // Where to spawn the structure.
                false, // "useExpansionHack" This is for legacy villages to generate properly. You should keep this false always.
                Optional.empty(), // Adds the terrain height's y value to the passed in blockpos's y value. (This uses WORLD_SURFACE_WG heightmap which stops at top water too)
                // Here at projectStartToHeightmap, start_height's y value is -1 which means the structure spawn -1 blocks below terrain height if start_height and project_start_to_heightmap is defined in structure JSON.
                // Set projectStartToHeightmap to be empty optional for structure to be place only at the passed in blockpos's Y value instead.
                // Definitely keep this an empty optional when placing structures in the nether as otherwise, heightmap placing will put the structure on the Bedrock roof.
                30, // Maximum limit for how far pieces can spawn from center. You cannot set this bigger than 128 or else pieces gets cutoff.
                StructurePoolAliasLookup.EMPTY, // Optional thing that allows swapping a template pool with another per structure json instance. We don't need this but see vanilla JigsawStructure class for how to wire it up if you want it.
                new DimensionPadding(this.depth, 0), // Optional thing to prevent generating too close to the bottom or top of the dimension.
                StructureLiquidSettings.IGNORE_WATERLOGGING);
    }

    @Override
    public StructureType<?> getType() {
        return Structures.SUSPICIOUS_LAKE_STRUCTURE;
    }
}
