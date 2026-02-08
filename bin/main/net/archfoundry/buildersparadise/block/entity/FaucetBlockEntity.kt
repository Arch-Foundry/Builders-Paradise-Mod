package net.archfoundry.buildersparadise.block.entity

import net.archfoundry.buildersparadise.registry.RegistryModule
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraftforge.common.capabilities.ForgeCapabilities

class FaucetBlockEntity(pos: BlockPos, state: BlockState) :
        BlockEntity(RegistryModule.FAUCET_BE.get(), pos, state) {

    var active = false
        set(value) {
            if (field != value) {
                field = value
                notifyUpdate()
            }
        }
    var cooldown = 0

    companion object {
        fun tick(
                level: net.minecraft.world.level.Level,
                pos: BlockPos,
                state: BlockState,
                entity: FaucetBlockEntity
        ) {
            if (level.isClientSide) return

            // Auto-activate if powered and idle
            if (!entity.active && state.getValue(BlockStateProperties.POWERED)) {
                if (entity.cooldown > 0) {
                    entity.cooldown--
                } else {
                    // Peek to see if we CAN transfer
                    if (canTransfer(level, pos, state)) {
                        entity.active = true
                    } else {
                        entity.cooldown = 20 // Wait 1 second before trying again
                    }
                }
            }

            if (entity.active) {
                if (entity.cooldown > 0) {
                    entity.cooldown--
                    return
                }
                entity.cooldown = 20 // Pour every second (placeholder speed)

                if (!doTransfer(level, pos, state)) {
                    entity.active = false
                }
            }
        }

        private fun canTransfer(
                level: net.minecraft.world.level.Level,
                pos: BlockPos,
                state: BlockState
        ): Boolean {
            val facing = state.getValue(BlockStateProperties.FACING)
            val backPos = pos.relative(facing.opposite)
            val sourceBe = level.getBlockEntity(backPos) ?: return false
            val sourceHandler =
                    sourceBe.getCapability(ForgeCapabilities.FLUID_HANDLER, facing).orElse(null)
                            ?: return false

            val belowPos = pos.below()
            val targetBe = level.getBlockEntity(belowPos) ?: return false
            val targetHandler =
                    targetBe.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.UP)
                            .orElse(null)
                            ?: return false

            val maxTransfer = 144
            val simulatedDrain =
                    sourceHandler.drain(
                            maxTransfer,
                            net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.SIMULATE
                    )

            if (simulatedDrain.amount <= 0) return false

            val filled =
                    targetHandler.fill(
                            simulatedDrain,
                            net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.SIMULATE
                    )
            return filled > 0
        }

        private fun doTransfer(
                level: net.minecraft.world.level.Level,
                pos: BlockPos,
                state: BlockState
        ): Boolean {
            val facing = state.getValue(BlockStateProperties.FACING)
            val backPos = pos.relative(facing.opposite)
            val sourceBe = level.getBlockEntity(backPos) ?: return false
            val sourceHandler =
                    sourceBe.getCapability(ForgeCapabilities.FLUID_HANDLER, facing).orElse(null)
                            ?: return false

            val belowPos = pos.below()
            val targetBe = level.getBlockEntity(belowPos) ?: return false
            val targetHandler =
                    targetBe.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.UP)
                            .orElse(null)
                            ?: return false

            // Transfer Logic
            val maxTransfer = 144
            val simulatedDrain =
                    sourceHandler.drain(
                            maxTransfer,
                            net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.SIMULATE
                    )

            if (simulatedDrain.amount > 0) {
                val filled =
                        targetHandler.fill(
                                simulatedDrain,
                                net.minecraftforge.fluids.capability.IFluidHandler.FluidAction
                                        .SIMULATE
                        )

                if (filled > 0) {
                    // Execute Transfer
                    val drained =
                            sourceHandler.drain(
                                    filled,
                                    net.minecraftforge.fluids.capability.IFluidHandler.FluidAction
                                            .EXECUTE
                            )
                    targetHandler.fill(
                            drained,
                            net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE
                    )
                    return true
                }
            }
            return false
        }
    }

    override fun saveAdditional(tag: CompoundTag) {
        super.saveAdditional(tag)
        tag.putBoolean("Active", active)
    }

    override fun load(tag: CompoundTag) {
        super.load(tag)
        active = tag.getBoolean("Active")
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
