package net.archfoundry.buildersparadise.block

import net.archfoundry.buildersparadise.block.entity.ForgeControllerBlockEntity
import net.archfoundry.buildersparadise.registry.RegistryModule
import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.DirectionProperty
import net.minecraft.world.phys.BlockHitResult

class ForgeControllerBlock(properties: BlockBehaviour.Properties) : BaseEntityBlock(properties) {

    companion object {
        val FACING: DirectionProperty = HorizontalDirectionalBlock.FACING
    }

    init {
        registerDefaultState(
                stateDefinition.any().setValue(FACING, net.minecraft.core.Direction.NORTH)
        )
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity? {
        return ForgeControllerBlockEntity(pos, state)
    }

    override fun getRenderShape(state: BlockState): RenderShape {
        return RenderShape.MODEL
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState? {
        return defaultBlockState().setValue(FACING, context.horizontalDirection.opposite)
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING)
    }

    override fun use(
            state: BlockState,
            level: Level,
            pos: BlockPos,
            player: Player,
            hand: InteractionHand,
            hit: BlockHitResult
    ): InteractionResult {
        if (level.isClientSide) return InteractionResult.SUCCESS

        val be = level.getBlockEntity(pos)
        if (be is ForgeControllerBlockEntity) {
            // For now, just trigger a scan or open GUI (placeholder)
            be.checkStructure()
            // player.openMenu(be) // GUI later
            return InteractionResult.CONSUME
        }
        return InteractionResult.PASS
    }

    override fun <T : BlockEntity?> getTicker(
            level: Level,
            state: BlockState,
            type: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        return createTickerHelper(
                type,
                RegistryModule.FORGE_CONTROLLER_BE.get(),
                ForgeControllerBlockEntity::tick
        )
    }
}
