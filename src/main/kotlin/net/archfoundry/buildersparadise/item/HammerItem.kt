package net.archfoundry.buildersparadise.item

import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

class HammerItem(properties: Properties) : Item(properties) {
    override fun hasCraftingRemainingItem(stack: ItemStack): Boolean {
        return true
    }

    override fun getCraftingRemainingItem(itemStack: ItemStack): ItemStack {
        val container = itemStack.copy()
        if (container.hurt(1, net.minecraft.util.RandomSource.create(), null)) {
            return ItemStack.EMPTY
        }
        return container
    }

    override fun isEnchantable(stack: ItemStack): Boolean {
        return true
    }
}
