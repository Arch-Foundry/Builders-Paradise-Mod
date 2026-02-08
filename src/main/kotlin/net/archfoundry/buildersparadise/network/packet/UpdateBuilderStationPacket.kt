package net.archfoundry.buildersparadise.network.packet

import net.archfoundry.buildersparadise.block.entity.BuilderStationBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.network.NetworkEvent
import java.util.function.Supplier

class UpdateBuilderStationPacket(private val pos: BlockPos, private val voxelData: CompoundTag) {

    companion object {
        fun encode(packet: UpdateBuilderStationPacket, buffer: FriendlyByteBuf) {
            buffer.writeBlockPos(packet.pos)
            buffer.writeNbt(packet.voxelData)
        }

        fun decode(buffer: FriendlyByteBuf): UpdateBuilderStationPacket {
            return UpdateBuilderStationPacket(buffer.readBlockPos(), buffer.readNbt() ?: CompoundTag())
        }

        fun handle(packet: UpdateBuilderStationPacket, ctx: Supplier<NetworkEvent.Context>) {
            ctx.get().enqueueWork {
                val player = ctx.get().sender
                val level = player?.level()
                if (level != null && level.isLoaded(packet.pos)) {
                    val blockEntity = level.getBlockEntity(packet.pos)
                    if (blockEntity is BuilderStationBlockEntity) {
                        blockEntity.setVoxelData(packet.voxelData)
                    }
                }
            }
            ctx.get().packetHandled = true
        }
    }
}
