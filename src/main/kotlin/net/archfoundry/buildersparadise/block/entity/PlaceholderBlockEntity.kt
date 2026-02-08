package net.archfoundry.buildersparadise.block.entity

import net.archfoundry.buildersparadise.registry.RegistryModule
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class PlaceholderBlockEntity(pos: BlockPos, state: BlockState) :
        BlockEntity(RegistryModule.PLACEHOLDER_BLOCK_BE.get(), pos, state) {

    private var voxelData: CompoundTag = CompoundTag()
    private var isAnchor: Boolean = false
    private var isValidStructure: Boolean = false

    fun setBlueprintData(data: CompoundTag) {
        this.voxelData = data
        this.isAnchor = true
        checkStructure()
        setChanged()
    }

    fun checkStructure() {
        if (level == null || voxelData.isEmpty) return

        var valid = true
        // Logic similar to old station logic, but centered on this block or relative to it.
        // For now, let's assume the Anchor is the "Bottom Center" or "Origin" of the blueprint.
        // We iterate the voxels and check the world relative to THIS block.

        voxelData.allKeys.forEach { xKey ->
            val x = xKey.toIntOrNull()
            if (x != null) {
                val xTag = voxelData.getCompound(xKey)
                xTag.allKeys.forEach { yKey ->
                    val y = yKey.toIntOrNull()
                    if (y != null) {
                        val yTag = xTag.getCompound(yKey)
                        yTag.allKeys.forEach { zKey ->
                            val z = zKey.toIntOrNull()
                            if (z != null) {
                                if (yTag.getBoolean(zKey)) {
                                    // Target Pos relative to Anchor
                                    // Let's assume Anchor is at (8, 0, 8) of the grid for
                                    // centering?
                                    // Or let's imply the blueprint grid (0..15) maps to world
                                    // coords relative to anchor.
                                    // Simple mapping: World = Anchor + (x-8, y, z-8)
                                    val targetPos = worldPosition.offset(x - 8, y, z - 8)

                                    val state = level!!.getBlockState(targetPos)
                                    if (!state.`is`(RegistryModule.PLACEHOLDER_BLOCK.get())) {
                                        valid = false
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        this.isValidStructure = valid
        // TODO: Sync to client for visualization if needed
    }

    override fun saveAdditional(tag: CompoundTag) {
        super.saveAdditional(tag)
        tag.put("Voxels", voxelData)
        tag.putBoolean("IsAnchor", isAnchor)
        tag.putBoolean("IsValid", isValidStructure)
    }

    override fun load(tag: CompoundTag) {
        super.load(tag)
        voxelData = tag.getCompound("Voxels")
        isAnchor = tag.getBoolean("IsAnchor")
        isValidStructure = tag.getBoolean("IsValid")
    }
}
