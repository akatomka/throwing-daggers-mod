package com.tomas.darts.item;

import com.tomas.darts.component.ModDataComponents;
import com.tomas.darts.enchantment.ModEnchantments;
import com.tomas.darts.entity.DartEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.List;

public class DartItem extends Item implements ProjectileItem {
    public static final int MAX_CHARGES = 3;
    private static final float BASE_CHARGE_SECONDS = 3.0f;
    private static final float MIN_CHARGE_SECONDS = 1.0f;
    // Vanilla Quick Charge tops out at level 3 (not 4), spread across its 3 levels so it
    // still bottoms out exactly at MIN_CHARGE_SECONDS at max level.
    private static final float CHARGE_SECONDS_PER_QUICK_CHARGE_LEVEL =
            (BASE_CHARGE_SECONDS - MIN_CHARGE_SECONDS) / 3.0f;

    private final DartMaterial material;

    public DartItem(DartMaterial material, Properties properties) {
        super(properties);
        this.material = material;
    }

    public DartMaterial getDartMaterial() {
        return material;
    }

    public static int getCharges(ItemStack stack) {
        CustomModelData data = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        Float value = data == null ? null : data.getFloat(0);
        return value == null ? MAX_CHARGES : Math.round(value);
    }

    public static void setCharges(ItemStack stack, int charges) {
        int clamped = Math.max(0, Math.min(MAX_CHARGES, charges));
        stack.set(DataComponents.CUSTOM_MODEL_DATA,
                new CustomModelData(List.of((float) clamped), List.of(), List.of(), List.of()));
    }

    public static void addCharge(ItemStack stack) {
        setCharges(stack, getCharges(stack) + 1);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        int charges = getCharges(stack);
        if (charges >= MAX_CHARGES) {
            return;
        }
        long now = level.getGameTime();
        Long nextChargeTick = stack.get(ModDataComponents.NEXT_CHARGE_TICK);
        if (nextChargeTick == null) {
            stack.set(ModDataComponents.NEXT_CHARGE_TICK, now + getChargeIntervalTicks(level, stack));
            return;
        }
        if (now >= nextChargeTick) {
            setCharges(stack, charges + 1);
            stack.set(ModDataComponents.NEXT_CHARGE_TICK, now + getChargeIntervalTicks(level, stack));
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return throwDart(level, player, hand);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        // Without this, right-clicking while aiming at an entity (a cow, a zombie, anything)
        // routes to entity interaction instead of Item#use, and the dart never throws.
        return throwDart(player.level(), player, hand);
    }

    private InteractionResult throwDart(Level level, Player player, InteractionHand hand) {
        ItemStack heldStack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel) {
            int charges = getCharges(heldStack);
            if (charges <= 0) {
                return InteractionResult.FAIL;
            }
            setCharges(heldStack, charges - 1);
            if (charges == MAX_CHARGES) {
                long now = serverLevel.getGameTime();
                heldStack.set(ModDataComponents.NEXT_CHARGE_TICK, now + getChargeIntervalTicks(level, heldStack));
            }

            ItemStack thrownStack = heldStack.copy();
            heldStack.hurtAndBreak(1, player, hand);

            Projectile.spawnProjectileFromRotation(
                    (spawnLevel, owner, projectileStack) -> {
                        DartEntity dart = new DartEntity(spawnLevel, owner, projectileStack);
                        dart.setDartDamage(material.damage());
                        return dart;
                    },
                    serverLevel, thrownStack, player, 0.0f, material.shootPower(), 1.0f);

            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
        return InteractionResult.SUCCESS;
    }

    private int getChargeIntervalTicks(Level level, ItemStack stack) {
        int quickChargeLevel = ModEnchantments.getLevel(level, stack, Enchantments.QUICK_CHARGE);
        float intervalSeconds = Math.max(MIN_CHARGE_SECONDS,
                BASE_CHARGE_SECONDS - CHARGE_SECONDS_PER_QUICK_CHARGE_LEVEL * quickChargeLevel);
        return Math.round(intervalSeconds * 20.0f);
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        DartEntity dart = new DartEntity(level, pos.x(), pos.y(), pos.z(), stack);
        dart.setDartDamage(material.damage());
        return dart;
    }
}
