package net.archfoundry.buildersparadise.block

import net.archfoundry.buildersparadise.block.entity.SmelteryBlockEntity
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
import net.minecraftforge.network.NetworkHooks

class SmelteryBlock(properties: Properties) : BaseEntityBlock(properties) {

    override fun newBlockEntity(p0: BlockPos, p1: BlockState): BlockEntity? {
        return SmelteryBlockEntity(p0, p1)
    }

    override fun getRenderShape(p0: BlockState): RenderShape {
        return RenderShape.MODEL
    }

    override fun <T : BlockEntity?> getTicker(
            level: Level,
            state: BlockState,
            type: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        return createTickerHelper(type, RegistryModule.SMELTERY_BE.get(), SmelteryBlockEntity::tick)
    }

    override fun use(
            state: BlockState,
            level: Level,
            pos: BlockPos,
            player: Player,
            hand: InteractionHand,
            hit: BlockHitResult
    ): InteractionResult {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS
        } else {
            val entity = level.getBlockEntity(pos)
            if (entity is SmelteryBlockEntity) {
                NetworkHooks.openScreen(
                        player as net.minecraft.server.level.ServerPlayer,
                        entity,
                        pos
                )
            }
            return InteractionResult.CONSUME
        }
    }
}
