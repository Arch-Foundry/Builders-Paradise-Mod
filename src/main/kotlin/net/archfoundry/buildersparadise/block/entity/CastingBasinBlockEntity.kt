package net.archfoundry.buildersparadise.block.entity

import net.archfoundry.buildersparadise.registry.RegistryModule
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.fluids.capability.templates.FluidTank
import net.minecraftforge.items.ItemStackHandler

class CastingBasinBlockEntity(pos: BlockPos, state: BlockState) :
        BlockEntity(RegistryModule.CASTING_BASIN_BE.get(), pos, state) {

    // Basin usually outputs a block, no cast slot, just output
    val itemHandler =
            object : ItemStackHandler(1) {
                override fun onContentsChanged(slot: Int) = notifyUpdate()
            }

    // Basin holds 1000mb (1 block) typically
    val fluidTank =
            object : FluidTank(1000) {
                override fun onContentsChanged() = notifyUpdate()
            }

    private val itemHandlerOptional = LazyOptional.of { itemHandler }
    private val fluidHandlerOptional = LazyOptional.of { fluidTank }

    override fun <T : Any?> getCapability(
            cap: Capability<T>,
            side: net.minecraft.core.Direction?
    ): LazyOptional<T> {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return itemHandlerOptional.cast()
        if (cap == ForgeCapabilities.FLUID_HANDLER) return fluidHandlerOptional.cast()
        return super.getCapability(cap, side)
    }

    // Logic
    var progress = 0
    var maxProgress = 100 // 5 seconds to cool

    companion object {
        fun tick(
                level: net.minecraft.world.level.Level,
                pos: BlockPos,
                state: BlockState,
                entity: CastingBasinBlockEntity
        ) {
            if (level.isClientSide) return

            val fluidAmount = entity.fluidTank.fluidAmount
            val fluidType = entity.fluidTank.fluid.fluid
            val outputSlot = entity.itemHandler.getStackInSlot(0)

            // Hardcoded Recipe: Molten Stone (1000mb) -> Stone Block
            val recipeFluid = RegistryModule.MOLTEN_STONE_SOURCE.get()
            val cost = 1000

            // Check if we have enough fluid and output is empty
            if (fluidAmount >= cost && fluidType == recipeFluid && outputSlot.isEmpty) {
                entity.progress++

                if (entity.progress >= entity.maxProgress) {
                    entity.progress = 0

                    // Consume Fluid
                    entity.fluidTank.drain(
                            cost,
                            net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE
                    )

                    // Create Item
                    entity.itemHandler.setStackInSlot(
                            0,
                            net.minecraft.world.item.ItemStack(
                                    net.minecraft.world.level.block.Blocks.STONE
                            )
                    )

                    setChanged(level, pos, state)
                }
            } else {
                entity.progress = 0
            }
        }
    }

    override fun saveAdditional(tag: CompoundTag) {
        super.saveAdditional(tag)
        tag.put("Inventory", itemHandler.serializeNBT())
        tag.put("Fluid", fluidTank.writeToNBT(CompoundTag()))
        tag.putInt("Progress", progress)
    }

    override fun load(tag: CompoundTag) {
        super.load(tag)
        itemHandler.deserializeNBT(tag.getCompound("Inventory"))
        fluidTank.readFromNBT(tag.getCompound("Fluid"))
        progress = tag.getInt("Progress")
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
