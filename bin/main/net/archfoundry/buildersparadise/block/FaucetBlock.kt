package net.archfoundry.buildersparadise.block

import net.archfoundry.buildersparadise.block.entity.FaucetBlockEntity
import net.archfoundry.buildersparadise.registry.RegistryModule
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

class FaucetBlock(properties: Properties) : BaseEntityBlock(properties) {

    init {
        registerDefaultState(
                stateDefinition
                        .any()
                        .setValue(BlockStateProperties.FACING, Direction.NORTH)
                        .setValue(BlockStateProperties.POWERED, false)
        )
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity? {
        return FaucetBlockEntity(pos, state)
    }

    override fun <T : BlockEntity?> getTicker(
            level: Level,
            state: BlockState,
            type: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        return createTickerHelper(type, RegistryModule.FAUCET_BE.get(), FaucetBlockEntity::tick)
    }

    override fun getRenderShape(state: BlockState): RenderShape {
        return RenderShape.MODEL
    }

    override fun use(
            state: BlockState,
            level: Level,
            pos: BlockPos,
            player: Player,
            hand: InteractionHand,
            hit: BlockHitResult
    ): InteractionResult {
        val be = level.getBlockEntity(pos)
        if (be is FaucetBlockEntity) {
            if (!level.isClientSide) {
                be.active = !be.active
                // Sound logic removed pending fix
                // val sound = if (be.active) SoundEvents.UI_BUTTON_CLICK else
                // SoundEvents.UI_BUTTON_CLICK
                // level.playSound(null as Player?, pos, sound, SoundSource.BLOCKS, 0.3f, 0.6f)
            }
            return InteractionResult.sidedSuccess(level.isClientSide)
        }
        return InteractionResult.PASS
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(BlockStateProperties.FACING, BlockStateProperties.POWERED)
    }

    override fun neighborChanged(
            state: BlockState,
            level: Level,
            pos: BlockPos,
            block: Block,
            fromPos: BlockPos,
            isMoving: Boolean
    ) {
        if (!level.isClientSide) {
            val isPowered = level.hasNeighborSignal(pos)
            val wasPowered = state.getValue(BlockStateProperties.POWERED)

            if (isPowered != wasPowered) {
                // State changed
                level.setBlock(pos, state.setValue(BlockStateProperties.POWERED, isPowered), 3)

                // Update Faucet Active State
                val be = level.getBlockEntity(pos)
                if (be is FaucetBlockEntity) {
                    be.active = isPowered
                }
            }
        }
    }

    // Placement Logic: Face away from the block it is placed on
    override fun getStateForPlacement(
            context: net.minecraft.world.item.context.BlockPlaceContext
    ): BlockState? {
        return defaultBlockState()
                .setValue(BlockStateProperties.FACING, context.clickedFace)
                .setValue(
                        BlockStateProperties.POWERED,
                        context.level.hasNeighborSignal(context.clickedPos)
                )
    }

    override fun getShape(
            state: BlockState,
            level: net.minecraft.world.level.BlockGetter,
            pos: BlockPos,
            context: CollisionContext
    ): VoxelShape {
        return when (state.getValue(BlockStateProperties.FACING)) {
            Direction.NORTH -> SHAPE_NORTH
            Direction.SOUTH -> SHAPE_SOUTH
            Direction.WEST -> SHAPE_WEST
            Direction.EAST -> SHAPE_EAST
            else -> SHAPE_NORTH
        }
    }

    companion object {
        // Attached to South (Z=16), pointing North (Z=0)
        // Attached to South (Z=16), pointing North (Z=0)
        // L-Shape: Arm (6, 6, 8) to (10, 10, 16) + Spout (6, 2, 6) to (10, 6, 10)
        // Bounding Box covering both:
        val SHAPE_NORTH = net.minecraft.world.level.block.Block.box(6.0, 2.0, 6.0, 10.0, 10.0, 16.0)
        // Attached to North (Z=0), pointing South (Z=16)
        val SHAPE_SOUTH = net.minecraft.world.level.block.Block.box(6.0, 2.0, 0.0, 10.0, 10.0, 10.0)
        // Attached to East (X=16), pointing West (X=0)
        val SHAPE_WEST = net.minecraft.world.level.block.Block.box(6.0, 2.0, 6.0, 16.0, 10.0, 10.0)
        // Attached to West (X=0), pointing East (X=16)
        val SHAPE_EAST = net.minecraft.world.level.block.Block.box(0.0, 2.0, 6.0, 10.0, 10.0, 10.0)
    }
}
