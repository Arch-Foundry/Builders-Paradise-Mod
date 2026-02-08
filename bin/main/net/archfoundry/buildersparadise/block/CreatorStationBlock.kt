package net.archfoundry.buildersparadise.block

import net.archfoundry.buildersparadise.block.entity.CreatorStationBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

class CreatorStationBlock(properties: Properties) : BaseEntityBlock(properties) {

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity? {
        return CreatorStationBlockEntity(pos, state)
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
        if (level.isClientSide) return InteractionResult.SUCCESS

        val blockEntity =
                level.getBlockEntity(pos) as? CreatorStationBlockEntity
                        ?: return InteractionResult.PASS
        val heldItem = player.getItemInHand(hand)

        if (!heldItem.isEmpty) {
            // Insertion Logic
            val item = heldItem.item
            var slot = -1
            if (item is net.archfoundry.buildersparadise.item.ToolPartItem && item.isProcessed) {
                slot =
                        when (item.partType) {
                            net.archfoundry.buildersparadise.item.ToolPartItem.PartType.HEAD -> 0
                            net.archfoundry.buildersparadise.item.ToolPartItem.PartType.AXE_HEAD ->
                                    0
                            net.archfoundry.buildersparadise.item.ToolPartItem.PartType
                                    .SHOVEL_HEAD -> 0
                            net.archfoundry.buildersparadise.item.ToolPartItem.PartType
                                    .SWORD_BLADE -> 0
                            net.archfoundry.buildersparadise.item.ToolPartItem.PartType.HANDLE -> 1
                            net.archfoundry.buildersparadise.item.ToolPartItem.PartType.BINDING -> 2
                        }
            }

            if (slot != -1) {
                val existing = blockEntity.inventory.getStackInSlot(slot)
                if (existing.isEmpty) {
                    val toInsert = heldItem.copy()
                    toInsert.count = 1
                    blockEntity.inventory.insertItem(slot, toInsert, false)
                    if (!player.isCreative) {
                        heldItem.shrink(1)
                    }
                    blockEntity.notifyUpdate()
                    return InteractionResult.CONSUME
                }
            }
        } else {
            // Extraction: Take from last non-empty slot (reverse order)
            for (i in 2 downTo 0) {
                if (!blockEntity.inventory.getStackInSlot(i).isEmpty) {
                    val extracted = blockEntity.inventory.extractItem(i, 64, false)
                    player.setItemInHand(hand, extracted)
                    blockEntity.notifyUpdate()
                    return InteractionResult.CONSUME
                }
            }
        }

        return InteractionResult.PASS
    }

    override fun <T : BlockEntity?> getTicker(
            level: Level,
            state: BlockState,
            type: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        // Ticker needed for visual projection validation?
        return null
    }
    init {
        registerDefaultState(
                stateDefinition
                        .any()
                        .setValue(
                                net.minecraft.world.level.block.state.properties
                                        .BlockStateProperties.HORIZONTAL_FACING,
                                net.minecraft.core.Direction.NORTH
                        )
        )
    }

    override fun getStateForPlacement(
            context: net.minecraft.world.item.context.BlockPlaceContext
    ): BlockState? {
        return defaultBlockState()
                .setValue(
                        net.minecraft.world.level.block.state.properties.BlockStateProperties
                                .HORIZONTAL_FACING,
                        context.horizontalDirection.opposite
                )
    }

    override fun createBlockStateDefinition(
            builder:
                    net.minecraft.world.level.block.state.StateDefinition.Builder<
                            net.minecraft.world.level.block.Block, BlockState>
    ) {
        builder.add(
                net.minecraft.world.level.block.state.properties.BlockStateProperties
                        .HORIZONTAL_FACING
        )
    }
}
