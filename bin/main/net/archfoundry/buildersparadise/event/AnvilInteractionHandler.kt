package net.archfoundry.buildersparadise.event

import net.archfoundry.buildersparadise.BuildersParadise
import net.archfoundry.buildersparadise.block.entity.ForgeAnvilBlockEntity
import net.archfoundry.buildersparadise.item.HammerItem
import net.archfoundry.buildersparadise.item.ToolPartItem
import net.archfoundry.buildersparadise.registry.RegistryModule
import net.minecraft.world.InteractionResult
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(modid = BuildersParadise.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
object AnvilInteractionHandler {

    @SubscribeEvent
    fun onAnvilRightClick(event: PlayerInteractEvent.RightClickBlock) {
        if (event.level.isClientSide) return

        val state = event.level.getBlockState(event.pos)
        // Check if it is a Vanilla Anvil (Any damage state)
        if (state.`is`(Blocks.ANVIL) ||
                        state.`is`(Blocks.CHIPPED_ANVIL) ||
                        state.`is`(Blocks.DAMAGED_ANVIL)
        ) {
            val player = event.entity
            val heldItem = player.getItemInHand(event.hand)

            // Condition to swap: Holding a Hammer or a Tool Part
            if (heldItem.item is HammerItem || heldItem.item is ToolPartItem) {
                // Perform Swap
                val facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING)
                val newBlock = RegistryModule.FORGE_ANVIL_BLOCK.get()
                val newState =
                        newBlock.defaultBlockState()
                                .setValue(
                                        BlockStateProperties.HORIZONTAL_FACING,
                                        facing
                                ) // Preserve Facing

                // Set the block
                event.level.setBlock(event.pos, newState, 3)

                // Transfer Item if it was a Part
                if (heldItem.item is ToolPartItem) {
                    val be = event.level.getBlockEntity(event.pos) as? ForgeAnvilBlockEntity
                    if (be != null) {
                        val toInsert = heldItem.copy()
                        toInsert.count = 1
                        be.inventory.insertItem(0, toInsert, false)

                        if (!player.isCreative) {
                            heldItem.shrink(1)
                        }
                        be.notifyUpdate()
                    }
                }

                // Cancel the original interaction so the anvil GUI doesn't open (though we replaced
                // the block so it shouldn't anyway)
                event.isCanceled = true
                event.cancellationResult = InteractionResult.SUCCESS
            }
        }
    }
}
