package com.tomas.darts.client;

import com.tomas.darts.client.render.DartRenderer;
import com.tomas.darts.entity.ModEntityTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class DartsModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(ModEntityTypes.DART, DartRenderer::new);
	}
}
