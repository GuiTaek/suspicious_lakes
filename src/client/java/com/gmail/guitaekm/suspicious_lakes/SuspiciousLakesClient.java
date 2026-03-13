package com.gmail.guitaekm.suspicious_lakes;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

public class SuspiciousLakesClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		FluidRenderHandlerRegistry.INSTANCE.register(
				SuspiciousLakes.SUSPICIOUS_LIQUID_STILL_FLUID,
				SuspiciousLakes.SUSPICIOUS_LIQUID_FLOWING_FLUID,
				new SuspiciousFluidRenderHandler(
						Identifier.of(SuspiciousLakes.MOD_ID, "block/activated_suspicious_liquid_still"),
						Identifier.of(SuspiciousLakes.MOD_ID, "block/activated_suspicious_liquid_flow"),
						Identifier.of(SuspiciousLakes.MOD_ID, "block/deactivated_suspicious_liquid_still"),
                        Identifier.of(SuspiciousLakes.MOD_ID, "block/deactivated_suspicious_liquid_flow")
				)
		);
		BlockRenderLayerMap.putFluids(
				BlockRenderLayer.TRANSLUCENT,
				SuspiciousLakes.SUSPICIOUS_LIQUID_STILL_FLUID,
				SuspiciousLakes.SUSPICIOUS_LIQUID_FLOWING_FLUID
		);
	}
}