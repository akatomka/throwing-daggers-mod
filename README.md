# Darts

A Fabric mod for Minecraft 26.2 that adds **throwing daggers** — a ranged, melee-tool-tier weapon in seven material tiers, with a charge system, combo damage, and three custom enchantments.

## Tiers

Each tier is built on its vanilla tool material, so durability and mining-relevant stats (enchantability, repair material) match that material exactly. Thrown damage and throw speed are tuned per tier for the mod's own balance.

| Tier | Thrown damage | Durability | Throw speed | Crafted with |
|---|---|---|---|---|
| Wooden | 3.0 | 59 | 1.4 | Wood planks |
| Stone | 4.0 | 131 | 1.4 | Cobblestone (stone tool materials) |
| Copper | 4.0 | 190 | 1.45 | Copper ingot |
| Iron | 5.0 | 250 | 1.5 | Iron ingot |
| Golden | 5.0 | 32 | 1.6 | Gold ingot |
| Diamond | 6.0 | 1561 | 1.5 | Diamond |
| Netherite | 7.0 | 2031 | 1.6 | Smithing upgrade from a diamond dagger |

Each tier crafts like its vanilla sword (2 material + 1 stick, sword-shaped), except Netherite, which upgrades a Diamond Throwing Dagger via a Netherite Upgrade Smithing Template + netherite ingot, same as any other netherite tool.

## How it plays

- **Right-click to throw.** Each dagger holds up to **3 charges**; throwing consumes one charge and one point of durability, and plays a sound like a thrown trident.
- **Charges regenerate over time** — 3 seconds per charge by default. The vanilla **Quick Charge** enchantment speeds this up, down to as little as 1 second per charge at Quick Charge III.
- **Melee damage is deliberately flat across every tier** (equal to your bare-handed punch), the same way vanilla's bow deals no bonus melee damage. All of a tier's power lives in its thrown damage instead, which is what the item tooltip shows.
- **Combos:** hitting the same target again within 2 seconds keeps a combo going, stacking up to 4 hits. Each hit past the first adds +1.0 damage (half a heart) on top of the dagger's base thrown damage.
- **Hat Trick:** landing the 3rd hit of a combo as a kill, on a target that was at full health when the combo started, triggers the challenge-tier **Hat Trick** advancement.

## Enchantments

| Enchantment | Max level | Effect |
|---|---|---|
| Toxic | II | On hit, poisons the target for 4 seconds (Poison I at level 1, Poison II at level 2). |
| Lag | II | On hit, slows the target for 1 second (Slowness I at level 1, Slowness II at level 2). |
| Rend | I | Passive: landing a Hat Trick kill while wielding a Rend-enchanted dagger instantly refills that dagger's charges to full. |

All three only apply to throwing daggers (`#darts:darts`) and are enchanting-table/anvil obtainable; Rend is additionally a rare bonus-enchanted-book drop from Bastion and End City chest loot.

## Advancements

- **Throwing Daggers** — craft any throwing dagger.
- **Bullseye** — hit an enemy with a thrown dagger.
- **Hat Trick** *(challenge)* — kill a mob at full health using only 3 daggers in quick succession.
- **Throwaway Diamonds** *(goal)* — craft a diamond throwing dagger.

## Requirements

- Minecraft 26.2
- Fabric Loader ≥ 0.19.3
- Fabric API 0.156.0+26.2
- Java 25+

## Installing

Drop the built jar (`darts-<version>.jar`) into your `mods/` folder alongside [Fabric API](https://modrinth.com/mod/fabric-api).

## Building from source

Requires JDK 25 (Gradle will pick up an installed JDK 25 automatically via its toolchain support; if none is found, install one — e.g. Temurin 25).

```
./gradlew build
```

The output jar is written to `build/libs/`.

## License

Available under the CC0 license — see [LICENSE](LICENSE).
