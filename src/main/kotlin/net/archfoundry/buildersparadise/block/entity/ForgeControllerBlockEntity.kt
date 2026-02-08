package net.archfoundry.buildersparadise.block.entity

import net.archfoundry.buildersparadise.block.ForgeControllerBlock
import net.archfoundry.buildersparadise.block.ForgedDrainBlock
import net.archfoundry.buildersparadise.block.ForgedTankBlock
import net.archfoundry.buildersparadise.config.ForgeConfig
import net.archfoundry.buildersparadise.registry.RegistryModule
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler

class ForgeControllerBlockEntity(pos: BlockPos, state: BlockState) :
        BlockEntity(RegistryModule.FORGE_CONTROLLER_BE.get(), pos, state) {

    var isValid = false
    var minPos: BlockPos = BlockPos.ZERO
    var maxPos: BlockPos = BlockPos.ZERO

    val slaves = java.util.HashSet<BlockPos>()

    // Inventory
    val itemHandler =
            object : net.minecraftforge.items.ItemStackHandler(9) {
                override fun onContentsChanged(slot: Int) {
                    setChanged()
                }
            }

    // Progress
    val meltingProgress = IntArray(9)
    val maxMeltingProgress = 100
    val fuelConsumption = 2 // mB per tick

    // Config Caching
    private val validFloorBlocks by lazy {
        ForgeConfig.INSTANCE.validFloorBlocks.get().map {
            net.minecraft.resources.ResourceLocation(it)
        }
    }
    private val validWallBlocks by lazy {
        ForgeConfig.INSTANCE.validWallBlocks.get().map {
            net.minecraft.resources.ResourceLocation(it)
        }
    }

    fun checkStructure() {
        if (level == null || level!!.isClientSide) return

        val facing = blockState.getValue(ForgeControllerBlock.FACING)
        // inside is "behind" the controller? No, usually Controller faces OUT. So inside is BEHIND
        // (Opposite).
        // Wait, Tcon controller faces OUT. So inside is BEHIND.
        val insideDir = facing.opposite

        // 1. Find opposite wall (define Depth)
        var depth = 0
        var foundOpposite = false
        for (i in 1..ForgeConfig.INSTANCE.maxSize.get() + 1) { // +1 for the wall itself
            val checkPos = worldPosition.relative(insideDir, i)
            val checkState = level!!.getBlockState(checkPos)
            if (isValidWall(checkState)) {
                depth = i - 1
                foundOpposite = true
                break
            }
            if (!checkState.isAir) { // Obstruction
                resetStructure()
                return
            }
        }
        if (!foundOpposite || depth < ForgeConfig.INSTANCE.minSize.get()) {
            resetStructure()
            return
        }

        // 2. Find Left/Right walls (define Width)
        val leftDir = insideDir.clockWise
        val rightDir = insideDir.counterClockWise

        // Let's assume the controller is centered-ish? No, could be anywhere on the wall.
        // Scan Left
        var leftDist = 0
        var foundLeft = false
        for (i in 1..ForgeConfig.INSTANCE.maxSize.get()) {
            val checkPos = worldPosition.relative(leftDir, i)
            val checkState = level!!.getBlockState(checkPos)
            if (isValidWall(checkState)) {
                leftDist = i - 1 // Distance to wall
                foundLeft = true
                break
            }
        }

        // Scan Right
        var rightDist = 0
        var foundRight = false
        for (i in 1..ForgeConfig.INSTANCE.maxSize.get()) {
            // We need to check if the width (Left+Right+1) exceeds max
            if (leftDist + i > ForgeConfig.INSTANCE.maxSize.get()) break

            val checkPos = worldPosition.relative(rightDir, i)
            val checkState = level!!.getBlockState(checkPos)
            if (isValidWall(checkState)) {
                rightDist = i - 1
                foundRight = true
                break
            }
        }

        if (!foundLeft || !foundRight) {
            resetStructure()
            return
        }

        val width = leftDist + 1 + rightDist
        if (width < ForgeConfig.INSTANCE.minSize.get()) {
            resetStructure()
            return
        }

        // 3. Define the Rectangle (Floor Level)
        // Controller is at worldPosition.
        // Corner 1: relative(leftDir, leftDist) + relative(insideDir, 1) ?
        // No, depth is "forward" from controller (insideDir).
        // Relative to controller:
        // Start: worldPosition + insideDir(1) + leftDir(leftDist)
        // End: worldPosition + insideDir(depth) + rightDir(rightDist)

        val p1 = worldPosition.relative(insideDir, 1).relative(leftDir, leftDist)
        val p2 = worldPosition.relative(insideDir, depth).relative(rightDir, rightDist)

        val innerMinX = minOf(p1.x, p2.x)
        val innerMaxX = maxOf(p1.x, p2.x)
        val innerMinZ = minOf(p1.z, p2.z)
        val innerMaxZ = maxOf(p1.z, p2.z)

        // 4. Validate Floor
        // Check blocks BELOW the rectangle (y-1)
        for (x in innerMinX..innerMaxX) {
            for (z in innerMinZ..innerMaxZ) {
                val floorPos = BlockPos(x, worldPosition.y - 1, z)
                if (!isValidFloor(level!!.getBlockState(floorPos))) {
                    resetStructure()
                    return
                }
            }
        }

        // 5. Detect Height (Scan Upwards)
        var height = 0
        for (h in 0 until ForgeConfig.INSTANCE.maxHeight.get()) {
            // Check Ring of Walls at y + h
            // Bounds: (innerMinX-1, innerMinZ-1) to (innerMaxX+1, innerMaxZ+1)
            // But only the border.
            // Also Controller must be part of the first layer (h=0)

            val y = worldPosition.y + h
            val isRingValid = checkWallRing(innerMinX, innerMaxX, innerMinZ, innerMaxZ, y)

            if (!isRingValid) {
                // If this layer fails, the valid height stops here.
                // But we need at least 1 layer?
                // Actually, if h=0 fails (where controller is), it's invalid.
                if (h == 0) {
                    resetStructure()
                    return
                }
                break
            }
            height = h + 1

            // Validate Air Inside
            if (!checkAirInside(innerMinX, innerMaxX, innerMinZ, innerMaxZ, y)) {
                resetStructure() // Interior must be clear
                return
            }
        }

        // Success!
        isValid = true
        minPos = BlockPos(innerMinX, worldPosition.y, innerMinZ)
        maxPos = BlockPos(innerMaxX, worldPosition.y + height - 1, innerMaxZ)
        println("Forge Structure Valid! Size: ${width}x${depth}x${height}")

        // Notify Slaves
        slaves.forEach { slavePos ->
            val be = level!!.getBlockEntity(slavePos)
            if (be is ForgedTankBlockEntity) be.masterPos = worldPosition
            if (be is ForgedDrainBlockEntity) be.masterPos = worldPosition
        }
    }

    private fun checkWallRing(minX: Int, maxX: Int, minZ: Int, maxZ: Int, y: Int): Boolean {
        for (x in minX - 1..maxX + 1) {
            for (z in minZ - 1..maxZ + 1) {
                // Determine if this is a border pos
                val isBorder = x == minX - 1 || x == maxX + 1 || z == minZ - 1 || z == maxZ + 1
                if (isBorder) {
                    val pos = BlockPos(x, y, z)
                    // If it is THIS controller (at h=0), it works.
                    if (pos == worldPosition) continue

                    val state = level!!.getBlockState(pos)
                    if (!isValidWall(state)) return false

                    // Add to slaves if it is one
                    if (state.block is ForgedTankBlock || state.block is ForgedDrainBlock
                    ) { // or other slaves
                        slaves.add(pos)
                    }
                }
            }
        }
        return true
    }

    private fun checkAirInside(minX: Int, maxX: Int, minZ: Int, maxZ: Int, y: Int): Boolean {
        for (x in minX..maxX) {
            for (z in minZ..maxZ) {
                val pos = BlockPos(x, y, z)
                if (!level!!.getBlockState(pos).isAir) return false
            }
        }
        return true
    }

    private fun resetStructure() {
        // Unlink previous slaves
        slaves.forEach { slavePos ->
            if (level != null) {
                val be = level!!.getBlockEntity(slavePos)
                if (be is ForgedTankBlockEntity) be.masterPos = null
                if (be is ForgedDrainBlockEntity) be.masterPos = null
            }
        }
        slaves.clear()

        isValid = false
        minPos = BlockPos.ZERO
        maxPos = BlockPos.ZERO
        println("Forge Structure Invalid.")
    }

    private fun isValidWall(state: BlockState): Boolean {
        val loc = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(state.block)
        return validWallBlocks.contains(loc)
    }

    private fun isValidFloor(state: BlockState): Boolean {
        val loc = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(state.block)
        return validFloorBlocks.contains(loc)
    }

    // Capability
    private val fluidHandlerOptional = LazyOptional.of { createMultiTankHandler() }
    private val itemHandlerOptional = LazyOptional.of { itemHandler }

    override fun <T : Any?> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            // Only return if valid structure
            if (isValid) return fluidHandlerOptional.cast()
        }
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandlerOptional.cast()
        }
        return super.getCapability(cap, side)
    }

    override fun invalidateCaps() {
        super.invalidateCaps()
        fluidHandlerOptional.invalidate()
        itemHandlerOptional.invalidate()
    }

    private fun createMultiTankHandler(): IFluidHandler {
        val tankBEs =
                slaves.mapNotNull { pos ->
                    val be = level!!.getBlockEntity(pos)
                    if (be is ForgedTankBlockEntity) be else null
                }

        return object : IFluidHandler {
            override fun getTanks(): Int = tankBEs.size

            override fun getFluidInTank(tank: Int): FluidStack {
                if (tank >= 0 && tank < tankBEs.size) {
                    return tankBEs[tank].fluidTank.fluid
                }
                return FluidStack.EMPTY
            }

            override fun getTankCapacity(tank: Int): Int {
                if (tank >= 0 && tank < tankBEs.size) {
                    return tankBEs[tank].fluidTank.capacity
                }
                return 0
            }

            override fun isFluidValid(tank: Int, stack: FluidStack): Boolean = true

            override fun fill(resource: FluidStack, action: IFluidHandler.FluidAction): Int {
                if (resource.isEmpty) return 0

                // Try to fill existing stacks first (merge)
                var remaining = resource.copy()

                // 1. Fill tanks that already have this fluid
                for (be in tankBEs) {
                    if (be.fluidTank.isFluidValid(remaining) &&
                                    be.fluidTank.fluid.isFluidEqual(remaining)
                    ) {
                        val filled = be.fluidTank.fill(remaining, action)
                        remaining.shrink(filled)
                        if (remaining.isEmpty) return resource.amount
                    }
                }

                // 2. Fill empty tanks
                if (!remaining.isEmpty) {
                    for (be in tankBEs) {
                        if (be.fluidTank.fluid.isEmpty) {
                            val filled = be.fluidTank.fill(remaining, action)
                            remaining.shrink(filled)
                            if (remaining.isEmpty) return resource.amount
                        }
                    }
                }

                return resource.amount - remaining.amount
            }

            override fun drain(
                    resource: FluidStack,
                    action: IFluidHandler.FluidAction
            ): FluidStack {
                if (resource.isEmpty) return FluidStack.EMPTY

                var drainedTotal = FluidStack.EMPTY.copy()
                var toDrain = resource.copy()

                for (be in tankBEs) {
                    if (be.fluidTank.fluid.isFluidEqual(toDrain)) {
                        val drained = be.fluidTank.drain(toDrain, action)
                        if (!drained.isEmpty) {
                            if (drainedTotal.isEmpty) {
                                drainedTotal = drained
                            } else {
                                drainedTotal.grow(drained.amount)
                            }
                            toDrain.shrink(drained.amount)
                            if (toDrain.isEmpty) break
                        }
                    }
                }
                return drainedTotal
            }

            override fun drain(maxDrain: Int, action: IFluidHandler.FluidAction): FluidStack {
                // Drain from the first available tank?
                for (be in tankBEs) {
                    if (!be.fluidTank.fluid.isEmpty) {
                        return be.fluidTank.drain(maxDrain, action)
                    }
                }
                return FluidStack.EMPTY
            }
        }
    }

    // Logic
    fun tickServer() {
        if (!isValid) return

        val fluidHandler = fluidHandlerOptional.orElse(null) as? IFluidHandler ?: return

        // Helper to consume fuel (Lava)
        fun consumeFuel(): Boolean {
            // Check if we have fuel (Molten Stone/Lava)
            val fuelRes = FluidStack(RegistryModule.MOLTEN_STONE_SOURCE.get(), fuelConsumption)
            val simulated = fluidHandler.drain(fuelRes, IFluidHandler.FluidAction.SIMULATE)
            if (simulated.amount >= fuelConsumption) {
                fluidHandler.drain(fuelRes, IFluidHandler.FluidAction.EXECUTE)
                return true
            }
            return false
        }

        // Process Slots
        for (i in 0 until itemHandler.slots) {
            val stack = itemHandler.getStackInSlot(i)
            if (stack.isEmpty) {
                meltingProgress[i] = 0
                continue
            }

            var recipeFluid: net.minecraft.world.level.material.Fluid? = null
            var recipeAmount = 1000

            // Hardcoded Recipes
            if (stack.`is`(net.minecraft.world.item.Items.COBBLESTONE)) {
                recipeFluid = RegistryModule.MOLTEN_STONE_SOURCE.get()
            } else if (stack.`is`(net.minecraft.world.item.Items.SAND)) {
                recipeFluid = RegistryModule.MOLTEN_GLASS_SOURCE.get()
            }

            if (recipeFluid != null) {
                // Check Output Space
                val outFluid = FluidStack(recipeFluid, recipeAmount)
                val filled = fluidHandler.fill(outFluid, IFluidHandler.FluidAction.SIMULATE)

                if (filled == recipeAmount) {
                    // Need Fuel to progress
                    if (consumeFuel()) {
                        meltingProgress[i]++
                        if (meltingProgress[i] >= maxMeltingProgress) {
                            // Complete
                            meltingProgress[i] = 0
                            stack.shrink(1)
                            itemHandler.setStackInSlot(i, stack) // Trigger update
                            fluidHandler.fill(outFluid, IFluidHandler.FluidAction.EXECUTE)
                        }
                    }
                } else {
                    // Tank full or invalid
                    meltingProgress[i] = 0
                }
            } else {
                meltingProgress[i] = 0
            }
        }
    }

    // Sync
    override fun getUpdateTag(): net.minecraft.nbt.CompoundTag {
        val tag = super.getUpdateTag()
        tag.putBoolean("isValid", isValid)
        if (isValid) {
            tag.putLong("minPos", minPos.asLong())
            tag.putLong("maxPos", maxPos.asLong())
        }
        return tag
    }

    override fun handleUpdateTag(tag: net.minecraft.nbt.CompoundTag) {
        super.handleUpdateTag(tag)
        isValid = tag.getBoolean("isValid")
        if (isValid) {
            minPos = BlockPos.of(tag.getLong("minPos"))
            maxPos = BlockPos.of(tag.getLong("maxPos"))
        }
    }

    override fun getUpdatePacket():
            net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket? {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this)
    }

    override fun onDataPacket(
            net: net.minecraft.network.Connection,
            pkt: net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
    ) {
        handleUpdateTag(pkt.tag ?: return)
    }

    companion object {
        fun tick(
                level: Level,
                pos: BlockPos,
                state: BlockState,
                entity: ForgeControllerBlockEntity
        ) {
            if (level.isClientSide) return

            // Structure Check Tick (Existing)
            if (level.gameTime % 80 == 0L) { // Check every 4 seconds
                val wasValid = entity.isValid
                entity.checkStructure()
                if (entity.isValid != wasValid) {
                    // Sync to client
                    level.sendBlockUpdated(pos, state, state, 3)
                    entity.setChanged()
                }
            }

            // Melting Logic
            entity.tickServer()
        }
    }
}
