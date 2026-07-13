package com.github.noamm9.packdisabler

import net.minecraft.client.Minecraft
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.Identifier
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

object DynamicItemModels {
    private val diamondSword = Items.DIAMOND_SWORD.identifier
    private val goldenSword = Items.GOLDEN_SWORD.identifier

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

    fun resolve(skyblockId: String, stack: ItemStack, customData: CompoundTag, fallback: Identifier) = when (skyblockId) {
        in attunedModels -> attunedModels.getValue(skyblockId).let { (mode, model) ->
            if (customData.getInt("td_attune_mode").orElse(- 1) == mode) model else fallback
        }

        in katanas -> if (Minecraft.getInstance().player?.cooldowns?.isOnCooldown(stack) == true) {
            goldenSword
        }
        else fallback

        in fungiCutters -> when (customData.getString("fungi_cutter_mode").orElse(null)) {
            "RED" -> Items.RED_MUSHROOM.identifier
            "BROWN" -> Items.BROWN_MUSHROOM.identifier
            else -> fallback
        }

        else -> fallback
    }

    private val Item.identifier get() = components().get(DataComponents.ITEM_MODEL) !!
}