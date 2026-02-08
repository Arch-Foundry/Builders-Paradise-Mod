package net.archfoundry.buildersparadise.item

import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level

class CastItem(properties: Properties, val cost: Int) : Item(properties) {
    override fun appendHoverText(
            stack: ItemStack,
            level: Level?,
            tooltipComponents: MutableList<Component>,
            isAdvanced: TooltipFlag
    ) {
        tooltipComponents.add(
                Component.literal("Cost: $cost mB").withStyle(net.minecraft.ChatFormatting.GRAY)
        )
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced)
    }
}
