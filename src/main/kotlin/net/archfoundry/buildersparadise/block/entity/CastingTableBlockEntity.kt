package net.archfoundry.buildersparadise.block.entity

import net.archfoundry.buildersparadise.item.CastItem
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

class CastingTableBlockEntity(pos: BlockPos, state: BlockState) :
        BlockEntity(RegistryModule.CASTING_TABLE_BE.get(), pos, state) {

        // Helper for holding Cast + Output
        val itemHandler =
                object : ItemStackHandler(2) {
                        override fun onContentsChanged(slot: Int) {
                                if (slot == 0) updateCapacity()
                                notifyUpdate()
                        }
                }

        // Tank for molten metal
        val fluidTank =
                object : FluidTank(144) {
                        override fun onContentsChanged() = notifyUpdate()

                        override fun fill(
                                resource: net.minecraftforge.fluids.FluidStack,
                                action:
                                        net.minecraftforge.fluids.capability.IFluidHandler.FluidAction
                        ): Int {
                                // If Output Slot (1) has an item, do not accept fluid
                                // Exception: unless it's the same fluid extending an existing cast?
                                // For now, strict: if output occupied, no fill.
                                if (!itemHandler.getStackInSlot(1).isEmpty) {
                                        return 0
                                }
                                return super.fill(resource, action)
                        }
                }

        private fun updateCapacity() {
                try {
                        val castStack = itemHandler.getStackInSlot(0)
                        val castItem = castStack.item
                        val newCapacity =
                                if (castItem is CastItem) castItem.cost else 144 // Default/Brick

                        // Safely update capacity
                        fluidTank.capacity = newCapacity
                } catch (e: Exception) {
                        System.err.println("Error updating CastingTable capacity: ${e.message}")
                        e.printStackTrace()
                }
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
        var maxProgress = 60 // 3 seconds to cool (smaller than block)

        companion object {
                fun tick(
                        level: net.minecraft.world.level.Level,
                        pos: BlockPos,
                        state: BlockState,
                        entity: CastingTableBlockEntity
                ) {
                        if (level.isClientSide) return

                        val fluidAmount = entity.fluidTank.fluidAmount
                        val fluidType = entity.fluidTank.fluid.fluid
                        val outputSlot = entity.itemHandler.getStackInSlot(1) // Slot 1 is Output

                        val inputSlot = entity.itemHandler.getStackInSlot(0) // Slot 0 is Cast/Input
                        val inputItem = inputSlot.item

                        // Determine Cost
                        val cost = if (inputItem is CastItem) inputItem.cost else 144

                        // Recipe Check
                        val outputItem = getRecipeOutput(inputSlot, fluidType, fluidAmount, cost)
                        val canSmelt = outputItem != null && outputSlot.isEmpty

                        if (canSmelt) {
                                entity.progress++

                                if (entity.progress >= entity.maxProgress) {
                                        entity.progress = 0

                                        // Consume Fluid
                                        entity.fluidTank.drain(
                                                cost,
                                                net.minecraftforge.fluids.capability.IFluidHandler
                                                        .FluidAction.EXECUTE
                                        )

                                        // Create Item in Slot 1 (Output)
                                        entity.itemHandler.setStackInSlot(1, outputItem!!)

                                        setChanged(level, pos, state)
                                }
                        } else {
                                entity.progress = 0
                        }
                }

                private fun getRecipeOutput(
                        inputStack: net.minecraft.world.item.ItemStack,
                        fluid: net.minecraft.world.level.material.Fluid,
                        amount: Int,
                        cost: Int
                ): net.minecraft.world.item.ItemStack? {
                        if (fluid != RegistryModule.MOLTEN_STONE_SOURCE.get()) return null
                        if (amount < cost) return null

                        // No Cast -> Brick (Placeholder)
                        if (inputStack.isEmpty)
                                return net.minecraft.world.item.ItemStack(
                                        net.minecraft.world.item.Items.BRICK
                                )

                        // Casts
                        val item = inputStack.item
                        if (item == RegistryModule.PICKAXE_HEAD_CAST.get())
                                return net.minecraft.world.item.ItemStack(
                                        RegistryModule.PICKAXE_HEAD.get()
                                )
                        if (item == RegistryModule.TOOL_HANDLE_CAST.get())
                                return net.minecraft.world.item.ItemStack(
                                        RegistryModule.TOOL_HANDLE.get()
                                )
                        if (item == RegistryModule.TOOL_BINDING_CAST.get())
                                return net.minecraft.world.item.ItemStack(
                                        RegistryModule.TOOL_BINDING.get()
                                )
                        if (item == RegistryModule.AXE_HEAD_CAST.get())
                                return net.minecraft.world.item.ItemStack(
                                        RegistryModule.AXE_HEAD.get()
                                )
                        if (item == RegistryModule.SHOVEL_HEAD_CAST.get())
                                return net.minecraft.world.item.ItemStack(
                                        RegistryModule.SHOVEL_HEAD.get()
                                )
                        if (item == RegistryModule.SWORD_BLADE_CAST.get())
                                return net.minecraft.world.item.ItemStack(
                                        RegistryModule.SWORD_BLADE.get()
                                )
                        if (item == RegistryModule.INGOT_CAST.get())
                                return net.minecraft.world.item.ItemStack(
                                        net.minecraft.world.item.Items.STONE_BRICKS
                                ) // Placeholder for Stone Ingot

                        // Gear Cast logic if we had a Gear Item
                        if (item == RegistryModule.GEAR_CAST.get()) {
                                // Return a Gear item (if we have one, otherwise maybe a diamond as
                                // placeholder?)
                                // For now return nothing or a placeholder
                                return net.minecraft.world.item.ItemStack(
                                        net.minecraft.world.item.Items.IRON_INGOT
                                ) // Placeholder
                        }

                        return null
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
                return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(
                        this
                )
        }

        private fun notifyUpdate() {
                setChanged()
                level?.sendBlockUpdated(worldPosition, blockState, blockState, 3)
        }
}
