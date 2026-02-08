package net.archfoundry.buildersparadise.compat.top

import java.util.function.Function
import mcjty.theoneprobe.api.IProbeHitData
import mcjty.theoneprobe.api.IProbeInfo
import mcjty.theoneprobe.api.IProbeInfoProvider
import mcjty.theoneprobe.api.ITheOneProbe
import mcjty.theoneprobe.api.ProbeMode
import net.archfoundry.buildersparadise.BuildersParadise
import net.archfoundry.buildersparadise.block.CastingTableBlock
import net.archfoundry.buildersparadise.block.SmelteryBlock
import net.archfoundry.buildersparadise.block.entity.CastingTableBlockEntity
import net.archfoundry.buildersparadise.block.entity.SmelteryBlockEntity
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

class TopPlugin : Function<ITheOneProbe, Void?> {
        override fun apply(probe: ITheOneProbe): Void? {
                // Smeltery Provider
                probe.registerProvider(
                        object : IProbeInfoProvider {
                                override fun getID(): ResourceLocation {
                                        return ResourceLocation(
                                                BuildersParadise.MOD_ID,
                                                "smeltery_provider"
                                        )
                                }

                                override fun addProbeInfo(
                                        mode: ProbeMode,
                                        probeInfo: IProbeInfo,
                                        player: Player,
                                        level: Level,
                                        state: BlockState,
                                        data: IProbeHitData
                                ) {
                                        if (state.block is SmelteryBlock) {
                                                val tile =
                                                        level.getBlockEntity(data.pos) as?
                                                                SmelteryBlockEntity
                                                                ?: return

                                                // Fluid
                                                val fluidAmount = tile.fluidTank.fluidAmount
                                                val fluidStack = tile.fluidTank.fluid
                                                if (!fluidStack.isEmpty) {
                                                        probeInfo.text(
                                                                net.minecraft.network.chat.Component
                                                                        .literal(
                                                                                "Fluid: ${fluidStack.displayName.string} ($fluidAmount mB)"
                                                                        )
                                                        )
                                                } else {
                                                        probeInfo.text(
                                                                net.minecraft.network.chat.Component
                                                                        .literal("Fluid: Empty")
                                                        )
                                                }

                                                // Fuel
                                                if (tile.fuelTime > 0) {
                                                        val seconds = tile.fuelTime / 20
                                                        probeInfo.text(
                                                                net.minecraft.network.chat.Component
                                                                        .literal(
                                                                                "Fuel: ${seconds}s"
                                                                        )
                                                        )
                                                }

                                                // Items (Input & Fuel)
                                                val inputStack = tile.itemHandler.getStackInSlot(0)
                                                val fuelStack = tile.itemHandler.getStackInSlot(1)

                                                if (!inputStack.isEmpty || !fuelStack.isEmpty) {
                                                        val itemsRow =
                                                                probeInfo.horizontal(
                                                                        probeInfo
                                                                                .defaultLayoutStyle()
                                                                                .borderColor(
                                                                                        0xffff0000
                                                                                                .toInt()
                                                                                )
                                                                )
                                                        if (!inputStack.isEmpty)
                                                                itemsRow.item(inputStack)
                                                        if (!fuelStack.isEmpty)
                                                                itemsRow.item(fuelStack)
                                                }
                                        }
                                }
                        }
                )

                // Casting Table Provider
                probe.registerProvider(
                        object : IProbeInfoProvider {
                                override fun getID(): ResourceLocation {
                                        return ResourceLocation(
                                                BuildersParadise.MOD_ID,
                                                "casting_table_provider"
                                        )
                                }

                                override fun addProbeInfo(
                                        mode: ProbeMode,
                                        probeInfo: IProbeInfo,
                                        player: Player,
                                        level: Level,
                                        state: BlockState,
                                        data: IProbeHitData
                                ) {
                                        if (state.block is CastingTableBlock) {
                                                val tile =
                                                        level.getBlockEntity(data.pos) as?
                                                                CastingTableBlockEntity
                                                                ?: return

                                                // Fluid
                                                val fluidAmount = tile.fluidTank.fluidAmount
                                                val fluidStack = tile.fluidTank.fluid
                                                if (!fluidStack.isEmpty) {
                                                        probeInfo.text(
                                                                net.minecraft.network.chat.Component
                                                                        .literal(
                                                                                "Fluid: ${fluidStack.displayName.string} ($fluidAmount mB)"
                                                                        )
                                                        )
                                                }

                                                // Items (Cast & Output)
                                                val castStack = tile.itemHandler.getStackInSlot(0)
                                                val outputStack = tile.itemHandler.getStackInSlot(1)

                                                if (!castStack.isEmpty || !outputStack.isEmpty) {
                                                        val itemsRow = probeInfo.horizontal()
                                                        if (!castStack.isEmpty) {
                                                                itemsRow.item(castStack)
                                                                itemsRow.text(
                                                                        net.minecraft.network.chat
                                                                                .Component.literal(
                                                                                " (Cast)"
                                                                        )
                                                                )
                                                        }
                                                        if (!outputStack.isEmpty) {
                                                                itemsRow.item(outputStack)
                                                        }
                                                }

                                                // Progress
                                                if (tile.progress > 0 && tile.maxProgress > 0) {
                                                        val pct =
                                                                (tile.progress * 100) /
                                                                        tile.maxProgress
                                                        probeInfo.progress(pct, 100)
                                                }
                                        }
                                }
                        }
                )

                // Casting Basin Provider
                probe.registerProvider(
                        object : IProbeInfoProvider {
                                override fun getID(): ResourceLocation {
                                        return ResourceLocation(
                                                BuildersParadise.MOD_ID,
                                                "casting_basin_provider"
                                        )
                                }

                                override fun addProbeInfo(
                                        mode: ProbeMode,
                                        probeInfo: IProbeInfo,
                                        player: Player,
                                        level: Level,
                                        state: BlockState,
                                        data: IProbeHitData
                                ) {
                                        if (state.block is
                                                        net.archfoundry.buildersparadise.block.CastingBasinBlock
                                        ) {
                                                val tile =
                                                        level.getBlockEntity(data.pos) as?
                                                                net.archfoundry.buildersparadise.block.entity.CastingBasinBlockEntity
                                                                ?: return

                                                // Fluid
                                                val fluidAmount = tile.fluidTank.fluidAmount
                                                val fluidStack = tile.fluidTank.fluid
                                                if (!fluidStack.isEmpty) {
                                                        probeInfo.text(
                                                                net.minecraft.network.chat.Component
                                                                        .literal(
                                                                                "Fluid: ${fluidStack.displayName.string} ($fluidAmount mB)"
                                                                        )
                                                        )
                                                }

                                                // Output Item
                                                val outputStack = tile.itemHandler.getStackInSlot(0)
                                                if (!outputStack.isEmpty) {
                                                        probeInfo.horizontal().item(outputStack)
                                                }

                                                // Progress
                                                if (tile.progress > 0 && tile.maxProgress > 0) {
                                                        val pct =
                                                                (tile.progress * 100) /
                                                                        tile.maxProgress
                                                        probeInfo.progress(pct, 100)
                                                }
                                        }
                                }
                        }
                )
                return null
        }
}
