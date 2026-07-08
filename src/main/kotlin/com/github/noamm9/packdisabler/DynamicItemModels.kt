package com.github.noamm9.packdisabler

import net.minecraft.client.Minecraft
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.Identifier
import net.minecraft.world.item.ItemStack

object DynamicItemModels {
    private val goldenSword = Identifier.withDefaultNamespace("golden_sword")
    private val diamondSword = Identifier.withDefaultNamespace("diamond_sword")
    private val attunedModels = mapOf(
        "FIREDUST_DAGGER" to (1 to goldenSword),
        "BURSTFIRE_DAGGER" to (1 to goldenSword),
        "HEARTFIRE_DAGGER" to (1 to goldenSword),
        "MAWDUST_DAGGER" to (3 to diamondSword),
        "BURSTMAW_DAGGER" to (3 to diamondSword),
        "HEARTMAW_DAGGER" to (3 to diamondSword),
    )
    private val katanas = setOf("VOIDEDGE_KATANA", "VORPAL_KATANA", "ATOMSPLIT_KATANA")
    private val fungiCutters = setOf("FUNGI_CUTTER", "FUNGI_CUTTER_2", "FUNGI_CUTTER_3")

    fun resolve(skyblockId: String, stack: ItemStack, customData: CompoundTag, fallback: Identifier): Identifier = when (skyblockId) {
        in attunedModels -> attunedModels.getValue(skyblockId).let { (mode, model) ->
            if (customData.getInt("td_attune_mode").orElse(-1) == mode) model else fallback
        }
        in katanas -> if (Minecraft.getInstance().player?.cooldowns?.isOnCooldown(stack) == true) {
            goldenSword
        } else fallback
        in fungiCutters -> when (customData.getString("fungi_cutter_mode").orElse(null)) {
            "RED" -> Identifier.withDefaultNamespace("red_mushroom")
            "BROWN" -> Identifier.withDefaultNamespace("brown_mushroom")
            else -> fallback
        }
        else -> fallback
    }
}
