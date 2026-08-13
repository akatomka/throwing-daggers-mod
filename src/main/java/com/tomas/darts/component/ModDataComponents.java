package com.tomas.darts.component;

import com.mojang.serialization.Codec;
import com.tomas.darts.DartsMod;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

public class ModDataComponents {
    // Game time at which this dart's next charge regenerates. Only meaningful while charges
    // are below the cap; irrelevant/stale once back at max.
    public static final DataComponentType<Long> NEXT_CHARGE_TICK = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            DartsMod.id("next_charge_tick"),
            DataComponentType.<Long>builder().persistent(Codec.LONG).build());

    public static void register() {
    }
}
