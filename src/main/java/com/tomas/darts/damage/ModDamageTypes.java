package com.tomas.darts.damage;

import com.tomas.darts.DartsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

public class ModDamageTypes {
    public static final ResourceKey<DamageType> DART = ResourceKey.create(Registries.DAMAGE_TYPE, DartsMod.id("dart"));
}
