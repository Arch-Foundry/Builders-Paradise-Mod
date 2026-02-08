package net.archfoundry.buildersparadise.item

import net.minecraft.world.item.Item

class ToolPartItem(properties: Properties, val partType: PartType, val isProcessed: Boolean) :
        Item(properties) {
    enum class PartType {
        HEAD,
        HANDLE,
        BINDING,
        AXE_HEAD,
        SHOVEL_HEAD,
        SWORD_BLADE
    }
}
