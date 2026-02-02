package com.gmail.guitaekm.enderlakes;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

public class EnderlakesClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		FluidRenderHandlerRegistry.INSTANCE.register(
				Enderlakes.SUSPICIOUS_LIQUID_STILL_FLUID,
				Enderlakes.SUSPICIOUS_LIQUID_FLOWING_FLUID,
				new SuspiciousFluidRenderHandler(
						Identifier.of(Enderlakes.MOD_ID, "block/activated_suspicious_liquid_still"),
						Identifier.of(Enderlakes.MOD_ID, "block/activated_suspicious_liquid_flow"),
						Identifier.of(Enderlakes.MOD_ID, "block/deactivated_suspicious_liquid_still"),
                        Identifier.of(Enderlakes.MOD_ID, "block/deactivated_suspicious_liquid_flow")
				)
		);
		BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), Enderlakes.SUSPICIOUS_LIQUID_STILL_FLUID, Enderlakes.SUSPICIOUS_LIQUID_FLOWING_FLUID);
	}
}