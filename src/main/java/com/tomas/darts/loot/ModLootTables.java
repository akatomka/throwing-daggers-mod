package com.tomas.darts.loot;

import com.tomas.darts.enchantment.ModEnchantments;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public class ModLootTables {
    // Rend is deliberately excluded from the enchanting table, villager trades, and generic
    // dungeon loot (see rend.json - it's just not added to any of those tags), the same way
    // vanilla gates Soul Speed/Swift Sneak. Instead it's injected directly into these two
    // structure loot tables, at a chance per chest rather than a guaranteed drop.
    private static final float REND_BOOK_CHANCE = 0.15f;
    private static final ResourceKey<LootTable> BASTION_TREASURE =
            ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.withDefaultNamespace("chests/bastion_treasure"));
    private static final ResourceKey<LootTable> END_CITY_TREASURE =
            ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.withDefaultNamespace("chests/end_city_treasure"));

    public static void register() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (!source.isBuiltin() || !(BASTION_TREASURE.equals(key) || END_CITY_TREASURE.equals(key))) {
                return;
            }
            Holder<Enchantment> rend = registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ModEnchantments.REND);
            tableBuilder.withPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1.0f))
                    .when(LootItemRandomChanceCondition.randomChance(REND_BOOK_CHANCE))
                    .add(LootItem.lootTableItem(Items.BOOK)
                            .apply(EnchantRandomlyFunction.randomEnchantment().withEnchantment(rend))));
        });
    }
}
