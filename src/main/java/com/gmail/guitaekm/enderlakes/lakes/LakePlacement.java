package com.gmail.guitaekm.enderlakes.lakes;

import com.gmail.guitaekm.enderlakes.Enderlakes;
import com.gmail.guitaekm.enderlakes.LakeDestinationFinder;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.gen.chunk.placement.*;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public class LakePlacement extends RandomSpreadStructurePlacement {

    public static final MapCodec<LakePlacement> CODEC = Codec.EMPTY.xmap(
            unit -> new LakePlacement(),
            lakePlacement -> Unit.INSTANCE
    );

    public LakePlacement() {
        super(
                new Vec3i(0, 0, 0),
                StructurePlacement.FrequencyReductionMethod.DEFAULT,
                1.0f,
                0,
                Optional.empty(),
                1,
                0,
                SpreadType.LINEAR
        );
    }

    @Override
    protected boolean isStartChunk(StructurePlacementCalculator calculator, int chunkX, int chunkZ) {
        // todo: switch to chunkRandom
        Set<LakeDestinationFinder.GridPos> positions = Enderlakes.finder.findNearestLake(
                calculator.getStructureSeed(),
                new ChunkPos(chunkX, chunkZ)
        );
        for (LakeDestinationFinder.GridPos pos : positions) {
            ChunkPos chunkPos = Enderlakes.finder.pos(calculator.getStructureSeed(), pos);
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
