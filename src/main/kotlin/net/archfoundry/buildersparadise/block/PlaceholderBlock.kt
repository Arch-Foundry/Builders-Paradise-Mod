package net.archfoundry.buildersparadise.block

import net.archfoundry.buildersparadise.item.BlueprintItem
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

class PlaceholderBlock(properties: Properties) : Block(properties), EntityBlock {

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity? {
        // We need to register the BlockEntity Type first!
        // For now, let's assume RegistryModule.PLACEHOLDER_BLOCK_BE exists (I will add it next).
        return net.archfoundry.buildersparadise.block.entity.PlaceholderBlockEntity(pos, state)
    }

    @Deprecated("Deprecated in Java")
    override fun use(
            state: BlockState,
            level: Level,
            pos: BlockPos,
            player: Player,
            hand: InteractionHand,
            hit: BlockHitResult
    ): InteractionResult {
        if (!level.isClientSide) {
            val heldItem = player.getItemInHand(hand)
            if (heldItem.item is BlueprintItem) {
                val tag = heldItem.tag
                if (tag != null && tag.contains("Voxels")) {
                    val be = level.getBlockEntity(pos)
                    if (be is net.archfoundry.buildersparadise.block.entity.PlaceholderBlockEntity
                    ) {
                        be.setBlueprintData(tag.getCompound("Voxels"))
                        player.sendSystemMessage(
                                Component.literal("Structure Data Transferred! Validating...")
                        )
                        // Visual feedback?
                    }
                } else {
                    player.sendSystemMessage(Component.literal("Blueprint is empty!"))
                }
                return InteractionResult.SUCCESS
            }
        }
        return super.use(state, level, pos, player, hand, hit)
    }
}
