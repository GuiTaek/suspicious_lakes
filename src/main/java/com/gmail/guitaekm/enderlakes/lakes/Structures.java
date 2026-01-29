package com.gmail.guitaekm.enderlakes.lakes;

import com.gmail.guitaekm.enderlakes.Enderlakes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.chunk.placement.StructurePlacementType;
import net.minecraft.world.gen.structure.StructureType;

public class Structures {
    public static StructurePlacementType<LakePlacement> LAKE_PLACEMENT = Registry.register(
            Registries.STRUCTURE_PLACEMENT,
            Identifier.of(Enderlakes.MOD_ID, "suspicious_lake_placement"),
            () -> LakePlacement.CODEC
    );

    public static StructurePieceType FEATURE_PIECE = Registry.register(
            Registries.STRUCTURE_PIECE,
            Identifier.of(Enderlakes.MOD_ID, "feature_structure_piece"),
            FeatureStructurePiece::new
    );

    public static StructureType<FeatureStructure> FEATURE_STRUCTURE = Registry.register(
            Registries.STRUCTURE_TYPE,
            Identifier.of(Enderlakes.MOD_ID, "feature_structure"),
            () -> FeatureStructure.CODEC
    );

    public static void load() { }
}
