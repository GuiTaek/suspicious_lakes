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
                    Vec3i
                            .createOffsetCodec(16)
                            .optionalFieldOf("locate_offset", Vec3i.ZERO)
                            .forGetter(LakePlacement::getLocateOffset),
                    StructurePlacement
                            .FrequencyReductionMethod
                            .CODEC
                            .optionalFieldOf(
                                    "frequency_reduction_method",
                                    StructurePlacement.FrequencyReductionMethod.DEFAULT
                            ).forGetter(LakePlacement::getFrequencyReductionMethod),
            Codec
                    .floatRange(0.0F, 1.0F)
                    .optionalFieldOf("frequency", 1.0F)
                    .forGetter(LakePlacement::getFrequency),
            Codecs
                    .NONNEGATIVE_INT
                    .fieldOf("salt")
                    .forGetter(LakePlacement::getSalt),
            StructurePlacement
                    .ExclusionZone
                    .CODEC
                    .optionalFieldOf("exclusion_zone")
                    .forGetter(LakePlacement::getExclusionZone),
            Codec
                    .intRange(0, Integer.MAX_VALUE)
                    .fieldOf("spacing")
                    .forGetter(LakePlacement::getSpacing),
            Codec
                    .intRange(0, Integer.MAX_VALUE)
                    .fieldOf("separation")
                    .forGetter(LakePlacement::getSeparation),
            SpreadType
                    .CODEC
                    .optionalFieldOf("spread_type", SpreadType.LINEAR)
                    .forGetter(LakePlacement::getSpreadType),
            Codec
                    .intRange(0, Integer.MAX_VALUE)
                    .fieldOf("g")
                    .forGetter(LakePlacement::g)

    ).apply(instance, instance.stable(LakePlacement::new)));
    public final int g;

    public int g() {
        return g;
    }

    public LakePlacement(
            Vec3i locateOffset,
            FrequencyReductionMethod frequencyReductionMethod,
            float frequency,
            int salt,
            Optional<ExclusionZone> exclusionZone,
            int spacing,
            int separation,
            SpreadType spreadType,
            int g
    ) {
        super(locateOffset, frequencyReductionMethod, frequency, salt, exclusionZone, spacing, separation, spreadType);
        this.g = g;
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
