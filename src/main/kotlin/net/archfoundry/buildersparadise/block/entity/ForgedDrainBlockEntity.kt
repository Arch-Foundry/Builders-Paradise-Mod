package net.archfoundry.buildersparadise.block.entity

import net.archfoundry.buildersparadise.registry.RegistryModule
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.common.util.LazyOptional

class ForgedDrainBlockEntity(pos: BlockPos, state: BlockState) :
        BlockEntity(RegistryModule.FORGED_DRAIN_BE.get(), pos, state) {

    var masterPos: BlockPos? = null

    override fun <T : Any?> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> {
        if (cap == ForgeCapabilities.FLUID_HANDLER && masterPos != null) {
            val master = level!!.getBlockEntity(masterPos!!)
            if (master is ForgeControllerBlockEntity) {
                return master.getCapability(cap, side)
            }
        }
        return super.getCapability(cap, side)
    }
}
