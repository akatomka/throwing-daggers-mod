package com.tomas.darts.item;

import com.tomas.darts.DartsMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ToolMaterial;

public enum DartMaterial {
    WOODEN(ToolMaterial.WOOD, "wooden", 3.0f, 1.4f,
            DartsMod.id("textures/entity/projectiles/dartprojectilewood.png")),
    STONE(ToolMaterial.STONE, "stone", 4.0f, 1.4f,
            DartsMod.id("textures/entity/projectiles/dartprojectilestone.png")),
    IRON(ToolMaterial.IRON, "iron", 5.0f, 1.5f,
            DartsMod.id("textures/entity/projectiles/dartprojectileiron.png")),
    GOLDEN(ToolMaterial.GOLD, "golden", 5.0f, 1.6f,
            DartsMod.id("textures/entity/projectiles/dartprojectilegold.png")),
    DIAMOND(ToolMaterial.DIAMOND, "diamond", 6.0f, 1.5f,
            DartsMod.id("textures/entity/projectiles/dartprojectilediamond.png")),
    NETHERITE(ToolMaterial.NETHERITE, "netherite", 7.0f, 1.6f,
            DartsMod.id("textures/entity/projectiles/dartprojectilenetherite.png"));

    private static final float BASE_UNARMED_ATTACK_DAMAGE = 1.0f;

    // Melee (M1) damage is deliberately uniform across every tier and matches vanilla's Bow,
    // which sets no attack-damage modifier at all (Items.BOW's Properties has no .sword()/
    // .tool()/.attributes() call) - so hitting with a bow, like hitting with a dagger, deals
    // just the player's base unarmed damage. Thrown damage (see damage()) is the real per-tier
    // number and is what the item tooltip displays instead of this.
    private static final float TARGET_MELEE_DAMAGE = BASE_UNARMED_ATTACK_DAMAGE;

    private final ToolMaterial toolMaterial;
    private final String tierName;
    private final float damage;
    private final float shootPower;
    private final ResourceLocation projectileTexture;

    DartMaterial(ToolMaterial toolMaterial, String tierName, float damage, float shootPower,
            ResourceLocation projectileTexture) {
        this.toolMaterial = toolMaterial;
        this.tierName = tierName;
        this.damage = damage;
        this.shootPower = shootPower;
        this.projectileTexture = projectileTexture;
    }

    public ToolMaterial toolMaterial() {
        return toolMaterial;
    }

    public String tierName() {
        return tierName;
    }

    /** Max thrown damage at full readiness - not used for melee swings. */
    public float damage() {
        return damage;
    }

    public float shootPower() {
        return shootPower;
    }

    /** Entity texture used while this dart is in flight/stuck; null falls back to vanilla's arrow texture. */
    public ResourceLocation projectileTexture() {
        return projectileTexture;
    }

    // Attack-damage modifier needed so the final melee damage (base unarmed 1.0 + this
    // modifier + the tool material's own attack bonus) comes out to exactly TARGET_MELEE_DAMAGE
    // regardless of tier, backing out each material's own attackDamageBonus().
    public float meleeAttributeDamage() {
        return TARGET_MELEE_DAMAGE - BASE_UNARMED_ATTACK_DAMAGE - toolMaterial.attackDamageBonus();
    }
}
