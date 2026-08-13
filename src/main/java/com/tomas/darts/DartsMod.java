package com.tomas.darts;

import com.tomas.darts.combo.ModAttachments;
import com.tomas.darts.component.ModDataComponents;
import com.tomas.darts.entity.ModEntityTypes;
import com.tomas.darts.item.ModItems;
import com.tomas.darts.loot.ModLootTables;
import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.ResourceLocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DartsMod implements ModInitializer {
	public static final String MOD_ID = "darts";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModDataComponents.register();
		ModAttachments.register();
		ModEntityTypes.register();
		ModItems.register();
		ModLootTables.register();

		LOGGER.info("Darts mod initializing!");
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}
