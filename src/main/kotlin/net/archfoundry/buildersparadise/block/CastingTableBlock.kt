package net.archfoundry.buildersparadise.block

import net.archfoundry.buildersparadise.block.entity.CastingTableBlockEntity
import net.archfoundry.buildersparadise.registry.RegistryModule
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
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

class CastingTableBlock(properties: Properties) : BaseEntityBlock(properties) {

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity? {
        return CastingTableBlockEntity(pos, state)
    }

    override fun <T : BlockEntity?> getTicker(
            level: Level,
            state: BlockState,
            type: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        return createTickerHelper(
                type,
                RegistryModule.CASTING_TABLE_BE.get(),
                CastingTableBlockEntity::tick
        )
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
        val be = level.getBlockEntity(pos)
        if (be is CastingTableBlockEntity) {
            // Check Output Slot (1)
            val outputStack = be.itemHandler.getStackInSlot(1)
            if (!outputStack.isEmpty) {
                // Take Output
                if (!player.inventory.add(outputStack)) {
                    player.drop(outputStack, false)
                }
                be.itemHandler.setStackInSlot(1, net.minecraft.world.item.ItemStack.EMPTY)
                level.playSound(
                        null,
                        pos,
                        net.minecraft.sounds.SoundEvents.ITEM_PICKUP,
                        net.minecraft.sounds.SoundSource.BLOCKS,
                        1.0f,
                        1.0f
                )
                return InteractionResult.CONSUME
            }
            // Check Cast Slot (0)
            val castStack = be.itemHandler.getStackInSlot(0)
            val handStack = player.getItemInHand(hand)

            if (handStack.isEmpty) {
                // Determine if we should take the cast
                // Only take cast if no output? (Output handled above)
                if (!castStack.isEmpty) {
                    if (!player.inventory.add(castStack)) {
                        player.drop(castStack, false)
                    }
                    be.itemHandler.setStackInSlot(0, net.minecraft.world.item.ItemStack.EMPTY)
                    level.playSound(
                            null,
                            pos,
                            net.minecraft.sounds.SoundEvents.ITEM_PICKUP,
                            net.minecraft.sounds.SoundSource.BLOCKS,
                            1.0f,
                            1.0f
                    )
                    return InteractionResult.CONSUME
                }
            } else {
                // Try to place cast
                if (castStack.isEmpty) {
                    val remainder = be.itemHandler.insertItem(0, handStack, false)
                    if (remainder.count < handStack.count) {
                        // Accepted
                        player.setItemInHand(hand, remainder)
                        level.playSound(
                                null,
                                pos,
                                net.minecraft.sounds.SoundEvents.ITEM_FRAME_ADD_ITEM,
                                net.minecraft.sounds.SoundSource.BLOCKS,
                                1.0f,
                                1.0f
                        )
                        return InteractionResult.CONSUME
                    }
                }
            }
        }
        return InteractionResult.PASS
    }

    // Tinkers' Table Shape (approximate)
    override fun getShape(
            state: BlockState,
            level: net.minecraft.world.level.BlockGetter,
            pos: BlockPos,
            context: CollisionContext
    ): VoxelShape {
        return net.minecraft.world.level.block.Block.box(
                0.0,
                0.0,
                0.0,
                16.0,
                16.0,
                16.0
        ) // Placeholder
    }
}
