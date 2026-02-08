package net.archfoundry.buildersparadise.menu

import net.archfoundry.buildersparadise.registry.RegistryModule
import net.minecraft.core.BlockPos
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.Container
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerData
import net.minecraft.world.inventory.SimpleContainerData
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

class BuilderStationMenu(
        containerId: Int,
        playerInventory: Inventory,
        val container: Container = SimpleContainer(1),
        private val data: ContainerData = SimpleContainerData(1),
        val blockPos: BlockPos = BlockPos.ZERO
) : AbstractContainerMenu(RegistryModule.BUILDER_STATION_MENU.get(), containerId) {

    // Client-side constructor
    constructor(
            containerId: Int,
            playerInventory: Inventory,
            extraData: FriendlyByteBuf
    ) : this(
            containerId,
            playerInventory,
            SimpleContainer(1),
            SimpleContainerData(1),
            extraData.readBlockPos()
    )

    // Server-side constructor convenience (though primary covers it)
    constructor(
            containerId: Int,
            playerInventory: Inventory
    ) : this(
            containerId,
            playerInventory,
            SimpleContainer(1),
            SimpleContainerData(1),
            BlockPos.ZERO
    )

    init {
        checkContainerDataCount(data, 1)
        checkContainerSize(container, 1)

        // Add Blueprint Slot (Index 0)
        // Moved to Right under buttons: X=175, Y=93 (18 + 75)
        this.addSlot(
                object : Slot(container, 0, 175, 93) {
                    override fun mayPlace(stack: ItemStack): Boolean {
                        return stack.`is`(RegistryModule.BLUEPRINT_ITEM.get())
                    }
                }
        )

        addDataSlots(data)

        // Add Player Hotbar slots
        // Centered: (210 - 162) / 2 = 24. Y=160
        for (k in 0..8) {
            this.addSlot(Slot(playerInventory, k, 24 + k * 18, 160))
        }
    }

    override fun quickMoveStack(player: Player, index: Int): ItemStack {
        var itemstack = ItemStack.EMPTY
        val slot = this.slots[index]
        if (slot != null && slot.hasItem()) {
            val itemstack1 = slot.item
            itemstack = itemstack1.copy()
            if (index == 0) { // Blueprint Slot
                // Target Hotbar only (Indices 1-9)
                if (!this.moveItemStackTo(itemstack1, 1, 10, true)) {
                    return ItemStack.EMPTY
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, 1, false)) { // Hotbar -> Blueprint Slot
                return ItemStack.EMPTY
            }

            if (itemstack1.isEmpty) {
                slot.set(ItemStack.EMPTY)
            } else {
                slot.setChanged()
            }

            if (itemstack1.count == itemstack.count) {
                return ItemStack.EMPTY
            }

            slot.onTake(player, itemstack1)
        }
        return itemstack
    }

    override fun stillValid(player: Player): Boolean {
        return container.stillValid(player)
    }

    val isStructureValid: Boolean
        get() = data.get(0) == 1
}
