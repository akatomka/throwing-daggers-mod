package com.tomas.darts.enchantment;

import com.tomas.darts.DartsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

public class ModEnchantments {
    public static final ResourceKey<Enchantment> TOXIC = key("toxic");
    public static final ResourceKey<Enchantment> LAG = key("lag");
    public static final ResourceKey<Enchantment> REND = key("rend");

    private static ResourceKey<Enchantment> key(String path) {
        return ResourceKey.create(Registries.ENCHANTMENT, DartsMod.id(path));
    }

    public static int getLevel(Level level, ItemStack stack, ResourceKey<Enchantment> key) {
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key)
                .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, stack))
                .orElse(0);
    }
}
