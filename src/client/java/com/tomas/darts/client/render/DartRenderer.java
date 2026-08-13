package com.tomas.darts.client.render;

import com.tomas.darts.entity.DartEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TippableArrowRenderer;
import net.minecraft.resources.Identifier;

public class DartRenderer extends ArrowRenderer<DartEntity, DartRenderState> {
    public DartRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public DartRenderState createRenderState() {
        return new DartRenderState();
    }

    @Override
    public void extractRenderState(DartEntity entity, DartRenderState renderState, float partialTick) {
        super.extractRenderState(entity, renderState, partialTick);
        Identifier texture = entity.getDartMaterial().projectileTexture();
        renderState.texture = texture != null ? texture : TippableArrowRenderer.NORMAL_ARROW_LOCATION;
    }

    @Override
    protected Identifier getTextureLocation(DartRenderState renderState) {
        return renderState.texture;
    }
}
