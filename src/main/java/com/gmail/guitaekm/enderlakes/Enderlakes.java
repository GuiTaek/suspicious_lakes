package com.gmail.guitaekm.enderlakes;

import com.gmail.guitaekm.enderlakes.lakes.Structures;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.*;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Enderlakes implements ModInitializer {
	public static final String MOD_ID = "suspicious_lakes";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final ConfigInstance CONFIG = new ConfigInstance().autoupdated();
    public static final LakeDestinationFinder finder = new LakeDestinationFinder(CONFIG);

	public static final FlowableFluid SUSPICIOUS_LIQUID_STILL_FLUID = Registry.register(
			Registries.FLUID,
			Identifier.of(MOD_ID, "suspicious_liquid"),
			new SuspiciousFluid.Still()
	);

	public static final FlowableFluid SUSPICIOUS_LIQUID_FLOWING_FLUID = Registry.register(
			Registries.FLUID,
			Identifier.of(MOD_ID, "flowing_suspicious_liquid"),
			new SuspiciousFluid.Flowing()
	);

	public static final SuspiciousFluidBlock SUSPICIOUS_LIQUID_BLOCK = Registry.register(
			Registries.BLOCK,
			Identifier.of(MOD_ID, "suspicious_liquid"),
			new SuspiciousFluidBlock(
					SUSPICIOUS_LIQUID_STILL_FLUID,
					AbstractBlock
							.Settings
							.create()
							.mapColor(MapColor.MAGENTA)
							.replaceable()
							.strength(100.0F)
							.pistonBehavior(PistonBehavior.DESTROY)
							.dropsNothing()
							.liquid()
							.sounds(BlockSoundGroup.AMETHYST_BLOCK)
							.dynamicBounds()
			));

	public static final Item SUSPICIOUS_BUCKET = Registry.register(
			Registries.ITEM,
			Identifier.of(MOD_ID, "suspicious_bucket"),
			new BucketItem(
					SUSPICIOUS_LIQUID_STILL_FLUID,
					new Item.Settings().recipeRemainder(Items.BUCKET).maxCount(1)
			)
	);

	public static final TagKey<Fluid> SUSPICIOUS_LIQUID = TagKey.of(RegistryKeys.FLUID, Identifier.of(MOD_ID, "suspicious_liquid"));
    public static final TagKey<EntityType<?>> PERMEABLE_BY_SUSPICIOUS_FLUID = TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID, "permeable_by_suspicious_fluid"));
    public static final TagKey<Biome> HAS_STRUCTURE_SUSPICIOUS_LAKE = TagKey.of(RegistryKeys.BIOME, Identifier.of(MOD_ID, "has_structure/suspicious_lake"));

	@Override
	public void onInitialize() {
        Structures.load();
        WorldBorderConfigUpdater.INSTANCE.register();
	}
}