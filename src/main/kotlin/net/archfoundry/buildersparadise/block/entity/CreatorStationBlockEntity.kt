package net.archfoundry.buildersparadise.block.entity

import net.archfoundry.buildersparadise.registry.RegistryModule
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.items.ItemStackHandler

class CreatorStationBlockEntity(pos: BlockPos, state: BlockState) :
        BlockEntity(RegistryModule.CREATOR_STATION_NEW_BE.get(), pos, state) {

    // 4 Slots: Head, Handle, Binding, Output (or just 3 input slots if output goes to anvil)
    val inventory =
            object : ItemStackHandler(3) {
                override fun onContentsChanged(slot: Int) {
                    setChanged()
                    notifyUpdate()
                }
            }
    private val optional = LazyOptional.of { inventory }

    override fun saveAdditional(tag: CompoundTag) {
        super.saveAdditional(tag)
        tag.put("inventory", inventory.serializeNBT())
    }

    override fun load(tag: CompoundTag) {
        super.load(tag)
        inventory.deserializeNBT(tag.getCompound("inventory"))
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener>? {
        return ClientboundBlockEntityDataPacket.create(this)
    }

    override fun getUpdateTag(): CompoundTag {
        return saveWithoutMetadata()
    }

    fun notifyUpdate() {
        level?.sendBlockUpdated(worldPosition, blockState, blockState, 3)
    }

    override fun <T : Any?> getCapability(
            cap: Capability<T>,
            side: net.minecraft.core.Direction?
    ): LazyOptional<T> {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return optional.cast()
        }
        return super.getCapability(cap, side)
    }

    override fun setRemoved() {
        super.setRemoved()
        optional.invalidate()
    }
}
