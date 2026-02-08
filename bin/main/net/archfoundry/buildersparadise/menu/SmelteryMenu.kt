package net.archfoundry.buildersparadise.menu

import net.archfoundry.buildersparadise.block.entity.SmelteryBlockEntity
import net.archfoundry.buildersparadise.registry.RegistryModule
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerData
import net.minecraft.world.inventory.SimpleContainerData
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.items.SlotItemHandler

class SmelteryMenu(
        containerId: Int,
        inv: Inventory,
        blockEntity: BlockEntity,
        private val data: ContainerData
) : AbstractContainerMenu(RegistryModule.SMELTERY_MENU.get(), containerId) {

    private val blockEntity: SmelteryBlockEntity = blockEntity as SmelteryBlockEntity

    constructor(
            containerId: Int,
            inv: Inventory,
            extraData: FriendlyByteBuf
    ) : this(
            containerId,
            inv,
            inv.player.level().getBlockEntity(extraData.readBlockPos())!!,
            SimpleContainerData(6) // Progress, MaxProgress, Fuel, MaxFuel, FluidAmount, FluidID
    )

    init {
        checkContainerDataCount(data, 6)

        // Block Entity Capability for Item Handler
        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent { handler ->
            // Input Slot (0) -> (56, 35)
            this.addSlot(SlotItemHandler(handler, 0, 56, 35))
            // Fuel Slot (1) -> (56, 53)
            this.addSlot(SlotItemHandler(handler, 1, 56, 53))
        }

        addDataSlots(data)

        // Player Inventory
        for (i in 0..2) {
            for (j in 0..8) {
                this.addSlot(Slot(inv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18))
            }
        }

        // Hotbar
        for (k in 0..8) {
            this.addSlot(Slot(inv, k, 8 + k * 18, 142))
        }
    }

    override fun quickMoveStack(player: Player, index: Int): ItemStack {
        var sourceStack = ItemStack.EMPTY
        val slot = this.slots[index]
        if (slot != null && slot.hasItem()) {
            val slotStack = slot.item
            sourceStack = slotStack.copy()

            // Smeltery Inventory (0, 1) -> Player Inventory
            if (index < 2) {
                if (!this.moveItemStackTo(slotStack, 2, 38, true)) {
                    return ItemStack.EMPTY
                }
            }
            // Player Inventory -> Smeltery
            else {
                // Check if it is fuel
                if (net.minecraftforge.common.ForgeHooks.getBurnTime(slotStack, null) > 0) {
                    if (!this.moveItemStackTo(slotStack, 1, 2, false)) {
                        // If fuel slot full, try input slot (maybe they want to smelt coal?)
                        if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                            return ItemStack.EMPTY
                        }
                    }
                } else {
                    // Not fuel, try input slot
                    if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY
                    }
                }
            }

            if (slotStack.isEmpty) {
                slot.set(ItemStack.EMPTY)
            } else {
                slot.setChanged()
            }

            if (slotStack.count == sourceStack.count) {
                return ItemStack.EMPTY
            }

            slot.onTake(player, slotStack)
        }
        return sourceStack
    }

    override fun stillValid(player: Player): Boolean {
        return stillValid(
                net.minecraft.world.inventory.ContainerLevelAccess.create(
                        blockEntity.level!!,
                        blockEntity.blockPos
                ),
                player,
                RegistryModule.SMELTERY_BLOCK.get()
        )
    }

    // Data Accessors for Screen
    val progress: Int
        get() = data.get(0)
    val maxProgress: Int
        get() = data.get(1)
    val fuelTime: Int
        get() = data.get(2)
    val maxFuelTime: Int
        get() = data.get(3)
    val fluidAmount: Int
        get() = data.get(4)
    val fluidId: Int
        get() = data.get(5)
}
