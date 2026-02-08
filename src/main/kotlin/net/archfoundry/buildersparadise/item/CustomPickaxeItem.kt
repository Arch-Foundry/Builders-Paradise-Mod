package net.archfoundry.buildersparadise.item

import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.PickaxeItem
import net.minecraft.world.item.Tiers

class CustomPickaxeItem(properties: Properties) : PickaxeItem(Tiers.IRON, 1, -2.8f, properties) {
    // Logic for loading stats from NBT will go here later.

    override fun getName(stack: ItemStack): Component {
        // Future: specific name based on materials (e.g. "Iron Pickaxe with Wood Handle")
        return super.getName(stack)
    }
}
