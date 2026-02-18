package com.gmail.guitaekm.suspicious_lakes;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class SuspiciousFluidRenderHandler implements FluidRenderHandler {
    public final Identifier activatedStillTextureId;
    public final Identifier activatedFlowingTextureId;
    public final Identifier deactivatedStillTextureId;
    public final Identifier deactivatedFlowingTextureId;
    public Map<Boolean, Sprite[]> sprites;

    public SuspiciousFluidRenderHandler(Identifier activatedStillTexture, Identifier activatedFlowingTexture, Identifier deactivatedStillTexture, Identifier deactivatedFlowingTexture) {
        this.activatedStillTextureId = activatedStillTexture;
        this.activatedFlowingTextureId = activatedFlowingTexture;
        this.deactivatedStillTextureId = deactivatedStillTexture;
        this.deactivatedFlowingTextureId = deactivatedFlowingTexture;
        this.sprites = new HashMap<>();
    }

    @Override
    public Sprite[] getFluidSprites(@Nullable BlockRenderView view, @Nullable BlockPos pos, FluidState state) {
        if (view == null) {
            return this.sprites.get(false);
        }
        return this.sprites.get(view.getBlockState(pos).get(SuspiciousFluidBlock.ACTIVATED));
    }

    @Override
    public void reloadTextures(SpriteAtlasTexture textureAtlas) {
        Sprite[] deactivated = new Sprite[] {
                textureAtlas.getSprite(deactivatedStillTextureId),
                textureAtlas.getSprite(deactivatedFlowingTextureId)
        };
        Sprite[] activated = new Sprite[] {
                textureAtlas.getSprite(activatedStillTextureId),
                textureAtlas.getSprite(activatedFlowingTextureId)
        };
        this.sprites.put(false, deactivated);
        this.sprites.put(true, activated);
    }
}
