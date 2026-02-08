package net.archfoundry.buildersparadise.block.entity

import net.archfoundry.buildersparadise.menu.SmelteryMenu
import net.archfoundry.buildersparadise.registry.RegistryModule
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.SimpleContainerData
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.fluids.capability.templates.FluidTank
import net.minecraftforge.items.ItemStackHandler

class SmelteryBlockEntity(pos: BlockPos, state: BlockState) :
        BlockEntity(RegistryModule.SMELTERY_BE.get(), pos, state), MenuProvider {

    val itemHandler =
            object : ItemStackHandler(2) {
                override fun onContentsChanged(slot: Int) {
                    setChanged()
                    notifyUpdate()
                }
            }

    // 4000mb = 4 Buckets
    val fluidTank =
            object : FluidTank(4000) {
                override fun onContentsChanged() {
                    setChanged()
                    notifyUpdate()
                }
            }

    private val itemHandlerOptional = LazyOptional.of { itemHandler }
    private val fluidHandlerOptional = LazyOptional.of { fluidTank }

    // Data sync
    protected val data =
            object : SimpleContainerData(6) {
                override fun get(index: Int): Int {
                    return when (index) {
                        0 -> progress
                        1 -> maxProgress
                        2 -> fuelTime
                        3 -> maxFuelTime
                        4 -> fluidTank.fluidAmount
                        5 -> 0 // Fluid ID placeholder
                        else -> 0
                    }
                }
                override fun set(index: Int, value: Int) {
                    when (index) {
                        0 -> progress = value
                        1 -> maxProgress = value
                        2 -> fuelTime = value
                        3 -> maxFuelTime = value
                    // Fluid sync usually one-way S->C via packet, but this helps simple sync
                    }
                }
            }

    // Logic variables
    var progress = 0
    var maxProgress = 100
    var fuelTime = 0
    var maxFuelTime = 0

    companion object {
        fun tick(level: Level, pos: BlockPos, state: BlockState, entity: SmelteryBlockEntity) {
            if (level.isClientSide) return

            var burning = entity.fuelTime > 0
            var changed = false

            if (entity.fuelTime > 0) {
                entity.fuelTime--
                // Ensure we update client if fuel runs out so animation stops
                if (entity.fuelTime == 0) {
                    println("Smeltery: Fuel ran out at ${pos}")
                    changed = true
                }
                if (entity.fuelTime % 200 == 0) {
                    println("Smeltery: Fuel remaining: ${entity.fuelTime}")
                }
            }

            val inputStack = entity.itemHandler.getStackInSlot(0)
            val fuelStack = entity.itemHandler.getStackInSlot(1)

            // Recipe Check (Hardcoded for now)
            val recipeFluid =
                    if (inputStack.`is`(net.minecraft.world.item.Items.COBBLESTONE))
                            RegistryModule.MOLTEN_STONE_SOURCE.get()
                    else null
            val recipeAmount = 1000 // 1 Block = 1000mB

            val canSmelt =
                    recipeFluid != null &&
                            entity.fluidTank.fill(
                                    net.minecraftforge.fluids.FluidStack(recipeFluid, recipeAmount),
                                    net.minecraftforge.fluids.capability.IFluidHandler.FluidAction
                                            .SIMULATE
                            ) == recipeAmount

            // Re-ignite
            if (entity.fuelTime == 0 && canSmelt && !fuelStack.isEmpty) {
                val burnTime = net.minecraftforge.common.ForgeHooks.getBurnTime(fuelStack, null)
                if (burnTime > 0) {
                    entity.fuelTime = burnTime
                    entity.maxFuelTime = burnTime

                    val containerItem =
                            if (fuelStack.hasCraftingRemainingItem())
                                    fuelStack.craftingRemainingItem
                            else net.minecraft.world.item.ItemStack.EMPTY

                    fuelStack.shrink(1)

                    if (fuelStack.isEmpty) {
                        entity.itemHandler.setStackInSlot(1, containerItem)
                    } else {
                        entity.itemHandler.setStackInSlot(1, fuelStack)
                    }

                    burning = true
                    changed = true
                }
            }

            // Progress
            if (entity.fuelTime > 0 && canSmelt) {
                entity.progress++
                if (entity.progress >= entity.maxProgress) {
                    entity.progress = 0
                    inputStack.shrink(1)
                    entity.itemHandler.setStackInSlot(0, inputStack)
                    entity.fluidTank.fill(
                            net.minecraftforge.fluids.FluidStack(recipeFluid!!, recipeAmount),
                            net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE
                    )
                    changed = true
                }
            } else {
                if (entity.progress > 0) {
                    entity.progress = (entity.progress - 2).coerceAtLeast(0)
                }
            }

            if (changed) {
                setChanged(level, pos, state)
                entity.notifyUpdate()
            }
        }
    }

    override fun getDisplayName(): Component {
        return Component.literal("Smeltery")
    }

    override fun createMenu(id: Int, inv: Inventory, player: Player): AbstractContainerMenu {
        return SmelteryMenu(id, inv, this, data)
    }

    override fun <T> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandlerOptional.cast()
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidHandlerOptional.cast()
        }
        return super.getCapability(cap, side)
    }

    override fun invalidateCaps() {
        super.invalidateCaps()
        itemHandlerOptional.invalidate()
        fluidHandlerOptional.invalidate()
    }

    override fun saveAdditional(tag: CompoundTag) {
        tag.put("inventory", itemHandler.serializeNBT())
        tag.put("fluid", fluidTank.writeToNBT(CompoundTag()))
        tag.putInt("progress", progress)
        tag.putInt("fuelTime", fuelTime)
        super.saveAdditional(tag)
    }

    override fun load(tag: CompoundTag) {
        super.load(tag)
        itemHandler.deserializeNBT(tag.getCompound("inventory"))
        fluidTank.readFromNBT(tag.getCompound("fluid"))
        progress = tag.getInt("progress")
        fuelTime = tag.getInt("fuelTime")
    }

    // Client Sync
    override fun getUpdateTag(): CompoundTag {
        return saveWithoutMetadata()
    }

    override fun getUpdatePacket():
            net.minecraft.network.protocol.Packet<
                    net.minecraft.network.protocol.game.ClientGamePacketListener>? {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this)
    }

    private fun notifyUpdate() {
        setChanged()
        level?.sendBlockUpdated(worldPosition, blockState, blockState, 3)
    }
}
