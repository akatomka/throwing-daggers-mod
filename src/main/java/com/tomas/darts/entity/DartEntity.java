package com.tomas.darts.entity;

import com.tomas.darts.DartsMod;
import com.tomas.darts.combo.ComboState;
import com.tomas.darts.combo.ModAttachments;
import com.tomas.darts.damage.ModDamageTypes;
import com.tomas.darts.enchantment.ModEnchantments;
import com.tomas.darts.item.DartItem;
import com.tomas.darts.item.DartMaterial;
import com.tomas.darts.item.ModItems;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

public class DartEntity extends AbstractArrow implements ItemSupplier {
    private static final int FLAT_TRAJECTORY_TICKS_PER_SHARPNESS_LEVEL = 3;

    // How long a combo stays alive without a follow-up hit, how much extra damage each hit
    // in a combo adds over the last one (half a heart = 1.0 raw damage), and the max number
    // of stacked hits.
    private static final int COMBO_WINDOW_TICKS = 40;
    private static final float COMBO_BONUS_PER_HIT = 1.0f;
    private static final int MAX_COMBO_HITS = 4;

    // AbstractArrow's own hit-block sound (the only impact sound our darts currently play -
    // a successful hit on a mob just plays the mob's own hurt sound, not a dart-specific one)
    // is hardcoded at full volume with no way to parameterize it, so we scale it down here.
    private static final float HIT_SOUND_VOLUME_SCALE = 0.6f;

    private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK =
            SynchedEntityData.defineId(DartEntity.class, EntityDataSerializers.ITEM_STACK);

    private final Set<Integer> hitEntityIds = new HashSet<>();

    private double dartDamage;
    private int flatTrajectoryTicks;

    public DartEntity(EntityType<? extends DartEntity> type, Level level) {
        super(type, level);
    }

    public DartEntity(Level level, LivingEntity owner, ItemStack stack) {
        super(ModEntityTypes.DART, owner, level, stack, stack);
        this.pickup = Pickup.DISALLOWED;
        this.entityData.set(DATA_ITEM_STACK, stack.copyWithCount(1));
        this.flatTrajectoryTicks = ModEnchantments.getLevel(level, stack, Enchantments.SHARPNESS)
                * FLAT_TRAJECTORY_TICKS_PER_SHARPNESS_LEVEL;
    }

    public DartEntity(Level level, double x, double y, double z, ItemStack stack) {
        super(ModEntityTypes.DART, x, y, z, level, stack, stack);
        this.pickup = Pickup.DISALLOWED;
        this.entityData.set(DATA_ITEM_STACK, stack.copyWithCount(1));
        this.flatTrajectoryTicks = ModEnchantments.getLevel(level, stack, Enchantments.SHARPNESS)
                * FLAT_TRAJECTORY_TICKS_PER_SHARPNESS_LEVEL;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ITEM_STACK, ItemStack.EMPTY);
    }

    public void setDartDamage(double damage) {
        this.dartDamage = damage;
        this.setBaseDamage(damage);
    }

    @Override
    public ItemStack getItem() {
        ItemStack synced = this.entityData.get(DATA_ITEM_STACK);
        return synced.isEmpty() ? getDefaultPickupItem() : synced;
    }

    public DartMaterial getDartMaterial() {
        ItemStack synced = this.entityData.get(DATA_ITEM_STACK);
        return synced.getItem() instanceof DartItem dartItem ? dartItem.getDartMaterial() : DartMaterial.WOODEN;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        // Called from AbstractArrow's own constructor, before firedFromWeapon (or our synced
        // item data) is set on this instance - must not depend on any per-instance state here.
        return new ItemStack(ModItems.WOODEN_DART);
    }

    @Override
    protected double getDefaultGravity() {
        double base = super.getDefaultGravity();
        if (this.flatTrajectoryTicks > 0 && this.tickCount < this.flatTrajectoryTicks) {
            return base * 0.1;
        }
        return base;
    }

    @Override
    public void playSound(SoundEvent soundEvent, float volume, float pitch) {
        super.playSound(soundEvent, volume * HIT_SOUND_VOLUME_SCALE, pitch);
    }

    @Override
    protected EntityHitResult findHitEntity(Vec3 start, Vec3 end) {
        // AbstractArrow's own findHitEntity uses ProjectileUtil.computeMargin(this), which
        // ramps the target hitbox's collision margin from 0 up to 0.3 blocks over this
        // entity's first 20 ticks (1s) of life - deliberately forgiving for arrows fired at
        // range, but it means a dart thrown at near-zero margin (tickCount 0-2) needs a
        // pixel-perfect ray/hitbox intersection to register at all. Darts are a close-range,
        // rapid-throw weapon by design - most throws land well inside that first second - so
        // we use vanilla's own steady-state margin unconditionally instead of ramping it up,
        // rather than inventing a new, unproven tolerance value.
        return ProjectileUtil.getEntityHitResult(this.level(), this, start, end,
                this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0), this::canHitEntity,
                ProjectileUtil.DEFAULT_ENTITY_HIT_RESULT_MARGIN);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        if (!this.hitEntityIds.add(target.getId())) {
            // Already hit this entity earlier in this dart's flight - AbstractArrow's own
            // pierce-tracking lives in a private field we can't reach from a subclass, so we
            // track it ourselves to avoid re-damaging the same target every tick it's still
            // overlapping the dart's hitbox.
            return;
        }

        Level level = this.level();
        Entity owner = this.getOwner();
        Holder<DamageType> dartDamageType =
                level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(ModDamageTypes.DART);
        DamageSource damageSource = new DamageSource(dartDamageType, this, owner != null ? owner : this);

        ComboState combo = owner != null ? updateCombo(level, owner, target) : ComboState.NONE;
        float comboBonus = combo.hits() > 0 ? (combo.hits() - 1) * COMBO_BONUS_PER_HIT : 0.0f;
        float damage = (float) this.dartDamage + comboBonus;
        if (level instanceof ServerLevel serverLevel) {
            damage = EnchantmentHelper.modifyDamage(serverLevel, this.getWeaponItem(), target, damageSource, damage);
        }
        boolean hurt = target.hurtOrSimulate(damageSource, damage);
        if (hurt) {
            if (level instanceof ServerLevel serverLevel) {
                EnchantmentHelper.doPostAttackEffectsWithItemSourceOnBreak(
                        serverLevel, target, damageSource, this.getWeaponItem(), this::onItemBreak);
            }
            if (target instanceof LivingEntity livingTarget) {
                this.doKnockback(livingTarget, damageSource);
                this.doPostHurtEffects(livingTarget);
                if (!livingTarget.isAlive() && combo.hits() == 3 && combo.startedAtFullHealth()
                        && owner instanceof ServerPlayer serverPlayer) {
                    awardHatTrick(serverPlayer);
                    applyRend(serverPlayer);
                }
            }
        }

        if (this.hitEntityIds.size() > this.getPierceLevel()) {
            this.discard();
        }
    }

    private static ComboState updateCombo(Level level, Entity attacker, Entity target) {
        if (!(attacker instanceof AttachmentTarget attachmentTarget)) {
            return ComboState.NONE;
        }
        long now = level.getGameTime();
        ComboState previous = attachmentTarget.getAttachedOrElse(ModAttachments.DART_COMBO, ComboState.NONE);
        boolean continuesCombo = previous.targetId() == target.getId() && now - previous.lastHitTick() <= COMBO_WINDOW_TICKS;
        int hits = continuesCombo ? Math.min(previous.hits() + 1, MAX_COMBO_HITS) : 1;
        boolean startedAtFullHealth = continuesCombo
                ? previous.startedAtFullHealth()
                : target instanceof LivingEntity livingTarget && livingTarget.getHealth() >= livingTarget.getMaxHealth();
        ComboState next = new ComboState(hits, now, target.getId(), startedAtFullHealth);
        attachmentTarget.setAttached(ModAttachments.DART_COMBO, next);
        return next;
    }

    private static void awardHatTrick(ServerPlayer serverPlayer) {
        AdvancementHolder advancement = serverPlayer.level().getServer().getAdvancements().get(DartsMod.id("hat_trick"));
        if (advancement != null) {
            serverPlayer.getAdvancements().award(advancement, "requirement");
        }
    }

    private void applyRend(ServerPlayer serverPlayer) {
        if (ModEnchantments.getLevel(this.level(), this.getWeaponItem(), ModEnchantments.REND) <= 0) {
            return;
        }
        ItemStack heldStack = serverPlayer.getMainHandItem();
        if (heldStack.getItem() instanceof DartItem) {
            DartItem.setCharges(heldStack, DartItem.MAX_CHARGES);
        }
    }

    @Override
    public void playerTouch(Player player) {
        // A dart that hit an entity already discards itself in onHitEntity; only a dart
        // that missed and stuck in the ground/a block ever reaches this. Deliberately does
        // NOT return the physical item - it only grants a charge to whatever dart the player
        // currently has in hand, per the charge-system design (see charge docs). Only darts
        // of the SAME tier as the one that landed get the charge, so e.g. stone darts can't
        // be used to farm charges on a netherite dart.
        if (this.level().isClientSide()) {
            return;
        }
        if (!this.isInGround() && !this.isNoPhysics()) {
            return;
        }
        if (this.shakeTime > 0) {
            return;
        }
        ItemStack heldStack = player.getMainHandItem();
        if (heldStack.getItem() instanceof DartItem dartItem && dartItem.getDartMaterial() == this.getDartMaterial()) {
            DartItem.addCharge(heldStack);
            this.discard();
        }
    }
}
