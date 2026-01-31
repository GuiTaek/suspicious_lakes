package com.gmail.guitaekm.enderlakes.lakes;

import com.gmail.guitaekm.enderlakes.Enderlakes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.chunk.placement.StructurePlacementType;
import net.minecraft.world.gen.structure.StructureType;

public class Structures {
    public static StructurePlacementType<LakePlacement> LAKE_PLACEMENT = Registry.register(
            Registries.STRUCTURE_PLACEMENT,
            Identifier.of(Enderlakes.MOD_ID, "suspicious_lake_placement"),
            () -> LakePlacement.CODEC
    );

    public static StructureType<SuspiciousLakeStructure> SUSPICIOUS_LAKE_STRUCTURE = Registry.register(
            Registries.STRUCTURE_TYPE,
            Identifier.of(Enderlakes.MOD_ID, "suspicious_lake_structure"),
            () -> SuspiciousLakeStructure.CODEC
    );

    public static void load() { }
}
