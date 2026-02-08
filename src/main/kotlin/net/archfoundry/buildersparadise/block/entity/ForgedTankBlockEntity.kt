package net.archfoundry.buildersparadise.block.entity

import net.archfoundry.buildersparadise.registry.RegistryModule
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.fluids.capability.templates.FluidTank

class ForgedTankBlockEntity(pos: BlockPos, state: BlockState) :
        BlockEntity(RegistryModule.FORGED_TANK_BE.get(), pos, state) {

    var masterPos: BlockPos? = null

    val fluidTank =
            object : FluidTank(4000) {
                override fun onContentsChanged() {
                    setChanged()
                    // Notify master if valid?
                }
            }

    // Add Capability handling later
}
