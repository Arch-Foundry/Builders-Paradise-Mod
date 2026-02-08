package net.archfoundry.buildersparadise.network

import net.archfoundry.buildersparadise.BuildersParadise
import net.archfoundry.buildersparadise.network.packet.UpdateBuilderStationPacket
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.network.NetworkRegistry
import net.minecraftforge.network.simple.SimpleChannel

object PacketHandler {
    private const val PROTOCOL_VERSION = "1"
    
    val CHANNEL: SimpleChannel = NetworkRegistry.newSimpleChannel(
        ResourceLocation(BuildersParadise.MOD_ID, "main"),
        { PROTOCOL_VERSION },
        { it == PROTOCOL_VERSION },
        { it == PROTOCOL_VERSION }
    )

    fun register() {
        var id = 0
        CHANNEL.registerMessage(
            id++,
            UpdateBuilderStationPacket::class.java,
            UpdateBuilderStationPacket::encode,
            UpdateBuilderStationPacket::decode,
            UpdateBuilderStationPacket::handle
        )
    }
}
