package com.gmail.guitaekm.enderlakes.lakes;

import com.gmail.guitaekm.enderlakes.Enderlakes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.LakeFeature;


public class Features {
    public static void load() { }
    @SuppressWarnings("deprecation")
    public static Feature<LakeFeature.Config> FORCED_LAKE_FEATURE = Registry.register(
            Registries.FEATURE,
            Identifier.of(Enderlakes.MOD_ID, "forced_lake"),
            new ForcedLakeFeature(LakeFeature.Config.CODEC)
    );
}
