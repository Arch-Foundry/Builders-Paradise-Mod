package net.archfoundry.buildersparadise.compat.jade

import net.archfoundry.buildersparadise.BuildersParadise
import net.archfoundry.buildersparadise.block.CastingTableBlock
import net.archfoundry.buildersparadise.block.SmelteryBlock
import net.archfoundry.buildersparadise.block.entity.CastingTableBlockEntity
import net.archfoundry.buildersparadise.block.entity.SmelteryBlockEntity
import net.minecraft.resources.ResourceLocation
import snownee.jade.api.BlockAccessor
import snownee.jade.api.IBlockComponentProvider
import snownee.jade.api.ITooltip
import snownee.jade.api.IWailaClientRegistration
import snownee.jade.api.IWailaCommonRegistration
import snownee.jade.api.IWailaPlugin
import snownee.jade.api.WailaPlugin
import snownee.jade.api.config.IPluginConfig

@WailaPlugin
class JadePlugin : IWailaPlugin {

    override fun register(registration: IWailaCommonRegistration) {
        // Common logic if any
    }

    override fun registerClient(registration: IWailaClientRegistration) {
        registration.registerBlockComponent(SmelteryProvider.INSTANCE, SmelteryBlock::class.java)
        registration.registerBlockComponent(
                CastingTableProvider.INSTANCE,
                CastingTableBlock::class.java
        )
    }
}

class SmelteryProvider : IBlockComponentProvider {
    companion object {
        val INSTANCE = SmelteryProvider()
    }

    override fun appendTooltip(tooltip: ITooltip, accessor: BlockAccessor, config: IPluginConfig) {
        val tile = accessor.blockEntity as? SmelteryBlockEntity ?: return

        // We need access to Smeltery data. Since it's client-side, we might rely on synced data or
        // NBT.
        // Usually Jade syncs NBT automatically if configured, or we read from ClientWorld BE.
        // SmelteryBlockEntity syncs via getUpdateTag / getUpdatePacket.

        // Fluid
        val fluidAmount = tile.fluidTank.fluidAmount
        val fluidStack = tile.fluidTank.fluid
        if (!fluidStack.isEmpty) {
            tooltip.add(
                    net.minecraft.network.chat.Component.literal(
                            "Fluid: ${fluidStack.displayName.string} ($fluidAmount mB)"
                    )
            )
        } else {
            tooltip.add(net.minecraft.network.chat.Component.literal("Fluid: Empty"))
        }

        // Fuel
        if (tile.fuelTime > 0) {
            val seconds = tile.fuelTime / 20
            tooltip.add(net.minecraft.network.chat.Component.literal("Fuel: ${seconds}s"))
        }
    }

    override fun getUid(): ResourceLocation {
        return ResourceLocation(BuildersParadise.MOD_ID, "smeltery_provider")
    }
}

class CastingTableProvider : IBlockComponentProvider {
    companion object {
        val INSTANCE = CastingTableProvider()
    }

    override fun appendTooltip(tooltip: ITooltip, accessor: BlockAccessor, config: IPluginConfig) {
        val tile = accessor.blockEntity as? CastingTableBlockEntity ?: return

        // Current Cast
        val castStack = tile.itemHandler.getStackInSlot(0)
        if (!castStack.isEmpty) {
            tooltip.add(
                    net.minecraft.network.chat.Component.literal(
                            "Cast: ${castStack.hoverName.string}"
                    )
            )
        }

        // Fluid
        val fluidAmount = tile.fluidTank.fluidAmount
        val fluidStack = tile.fluidTank.fluid
        if (!fluidStack.isEmpty) {
            tooltip.add(
                    net.minecraft.network.chat.Component.literal(
                            "Fluid: ${fluidStack.displayName.string} ($fluidAmount mB)"
                    )
            )
        }

        // Progress (Cooling)
        // We assume 'progress' field is public or accessible via accessor if we exposed it.
        // CastingTableBlockEntity has `progress` variable.
        if (tile.progress > 0 && tile.maxProgress > 0) {
            val pct = (tile.progress * 100) / tile.maxProgress
            tooltip.add(net.minecraft.network.chat.Component.literal("Cooling: $pct%"))
        }
    }

    override fun getUid(): ResourceLocation {
        return ResourceLocation(BuildersParadise.MOD_ID, "casting_table_provider")
    }
}
