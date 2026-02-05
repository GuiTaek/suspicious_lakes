package com.gmail.guitaekm.enderlakes.lakes;

import com.gmail.guitaekm.enderlakes.Enderlakes;
import com.gmail.guitaekm.enderlakes.LakeDestinationFinder;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.gen.chunk.placement.*;

import java.util.Optional;
import java.util.Set;

public class LakePlacement extends RandomSpreadStructurePlacement {

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
                new ChunkPos(chunkX, chunkZ)
        );
        for (LakeDestinationFinder.GridPos pos : positions) {
            ChunkPos chunkPos = LakeDestinationFinder
                    .pos(Enderlakes.CONFIG, calculator.getStructureSeed() + this.getSalt(), pos);
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
