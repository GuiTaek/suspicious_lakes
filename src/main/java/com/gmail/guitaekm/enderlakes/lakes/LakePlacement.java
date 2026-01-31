package com.gmail.guitaekm.enderlakes.lakes;

import com.gmail.guitaekm.enderlakes.Enderlakes;
import com.gmail.guitaekm.enderlakes.LakeDestinationFinder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.gen.chunk.placement.*;

import java.util.Optional;
import java.util.Set;

public class LakePlacement extends RandomSpreadStructurePlacement {


    // Special codec where we tacked on a "min_distance_from_world_origin" field so
    // we can now have structures spawn based on distance from world center.
    public static final MapCodec<LakePlacement> CODEC = RecordCodecBuilder.mapCodec(
            (instance) -> instance.group(
            Codecs
                    .NONNEGATIVE_INT
                    .fieldOf("salt")
                    .forGetter(LakePlacement::getSalt)

    ).apply(instance, instance.stable(LakePlacement::new)));

    public LakePlacement(int salt) {
        super(new Vec3i(0, 0, 0), StructurePlacement.FrequencyReductionMethod.DEFAULT, 1.0f, salt, Optional.empty(), 1, 0, SpreadType.LINEAR);
    }

    @Override
    protected boolean isStartChunk(StructurePlacementCalculator calculator, int chunkX, int chunkZ) {
        // todo: switch to chunkRandom
        Set<LakeDestinationFinder.GridPos> positions = LakeDestinationFinder.findNearestLake(
                Enderlakes.CONFIG,
                calculator.getStructureSeed() + this.getSalt(),
                new LakeDestinationFinder.ChunkPos(chunkX, chunkZ)
        );
        for (LakeDestinationFinder.GridPos pos : positions) {
            ChunkPos chunkPos = LakeDestinationFinder
                    .pos(Enderlakes.CONFIG, calculator.getStructureSeed() + this.getSalt(), pos)
                    .toMinecraft();
            if (chunkPos.x == chunkX && chunkPos.z == chunkZ) {
                return true;
            }
        }
        return false;
    }

    @Override
    public StructurePlacementType<?> getType() {
        return super.getType();
    }
}
