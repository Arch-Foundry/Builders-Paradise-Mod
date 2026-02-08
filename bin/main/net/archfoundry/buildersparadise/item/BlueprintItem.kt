package net.archfoundry.buildersparadise.item

import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import org.jetbrains.annotations.Nullable

class BlueprintItem(properties: Properties) : Item(properties) {
    override fun appendHoverText(
            stack: ItemStack,
            level: @Nullable Level?,
            tooltipComponents: MutableList<Component>,
            isAdvanced: TooltipFlag
    ) {
        val tag = stack.tag
        if (tag != null && tag.contains("Voxels")) {
            tooltipComponents.add(
                    Component.translatable("item.buildersparadise.blueprint.tooltip.data")
            )
        } else {
            tooltipComponents.add(
                    Component.translatable("item.buildersparadise.blueprint.tooltip.empty")
            )
        }
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced)
    }
}
