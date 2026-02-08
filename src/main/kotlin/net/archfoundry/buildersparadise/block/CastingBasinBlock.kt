package net.archfoundry.buildersparadise.block

import net.archfoundry.buildersparadise.block.entity.CastingBasinBlockEntity
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

class CastingBasinBlock(properties: Properties) : BaseEntityBlock(properties) {

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity? {
        return CastingBasinBlockEntity(pos, state)
    }

    override fun <T : BlockEntity?> getTicker(
            level: Level,
            state: BlockState,
            type: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        return createTickerHelper(
                type,
                RegistryModule.CASTING_BASIN_BE.get(),
                CastingBasinBlockEntity::tick
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
        if (be is CastingBasinBlockEntity) {
            val outputStack = be.itemHandler.getStackInSlot(0)
            if (!outputStack.isEmpty) {
                // Take Item
                if (!player.inventory.add(outputStack)) {
                    player.drop(outputStack, false)
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
        }
        return InteractionResult.PASS
    }
}
