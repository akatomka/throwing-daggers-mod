package com.tomas.darts.item;

import com.tomas.darts.DartsMod;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.List;

public class ModItems {
    private static final float ATTACK_SPEED_MODIFIER = -2.4f;

    public static final Item WOODEN_DART = register(DartMaterial.WOODEN);
    public static final Item STONE_DART = register(DartMaterial.STONE);
    public static final Item IRON_DART = register(DartMaterial.IRON);
    public static final Item GOLDEN_DART = register(DartMaterial.GOLDEN);
    public static final Item DIAMOND_DART = register(DartMaterial.DIAMOND);
    public static final Item NETHERITE_DART = register(DartMaterial.NETHERITE);

    private static Item register(DartMaterial material) {
        String path = material.tierName() + "_dart";
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, DartsMod.id(path));
        Item.Properties properties = new Item.Properties()
                .setId(key)
                .sword(material.toolMaterial(), material.meleeAttributeDamage(), ATTACK_SPEED_MODIFIER)
                .attributes(attributesWithThrownDamageTooltip(material))
                .component(DataComponents.CUSTOM_MODEL_DATA,
                        new CustomModelData(List.of((float) DartItem.MAX_CHARGES), List.of(), List.of(), List.of()));
        return Registry.register(BuiltInRegistries.ITEM, key, new DartItem(material, properties));
    }

    // Same modifiers vanilla's sword() builder sets (melee math unchanged) - only the attack-damage tooltip line is overridden to show thrown damage instead.
    private static ItemAttributeModifiers attributesWithThrownDamageTooltip(DartMaterial material) {
        Component thrownDamageTooltip = Component.translatable("attribute.modifier.plus.0",
                        ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(material.damage()),
                        Component.translatable(Attributes.ATTACK_DAMAGE.value().getDescriptionId()))
                .withStyle(ChatFormatting.DARK_GREEN);
        // meleeAttributeDamage() subtracts toolMaterial().attackDamageBonus() so that when
        // vanilla's own sword() builder re-adds it internally, the two cancel out. We're not
        // going through sword()'s builder here (we need the Display override below), so we
        // have to re-add that bonus back ourselves or every tier above wood/gold ends up with
        // a zero-or-negative attack-damage attribute - and vanilla's Player#attack() silently
        // skips the entire attack (no damage, no sound, no knockback) whenever that happens.
        float meleeDamageModifier = material.meleeAttributeDamage() + material.toolMaterial().attackDamageBonus();
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, meleeDamageModifier, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND,
                        ItemAttributeModifiers.Display.override(thrownDamageTooltip))
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, ATTACK_SPEED_MODIFIER, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .build();
    }

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(entries -> {
            entries.accept(WOODEN_DART);
            entries.accept(STONE_DART);
            entries.accept(IRON_DART);
            entries.accept(GOLDEN_DART);
            entries.accept(DIAMOND_DART);
            entries.accept(NETHERITE_DART);
        });
    }
}
