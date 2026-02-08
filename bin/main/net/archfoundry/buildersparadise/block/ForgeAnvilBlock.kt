package net.archfoundry.buildersparadise.block

import net.archfoundry.buildersparadise.block.entity.ForgeAnvilBlockEntity
import net.archfoundry.buildersparadise.item.HammerItem
import net.archfoundry.buildersparadise.item.ToolPartItem
import net.archfoundry.buildersparadise.registry.RegistryModule
import net.minecraft.core.BlockPos
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.AnvilBlock
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

class ForgeAnvilBlock(properties: Properties) : AnvilBlock(properties), EntityBlock {

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity? {
        return ForgeAnvilBlockEntity(pos, state)
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
        }

        val blockEntity =
                level.getBlockEntity(pos) as? ForgeAnvilBlockEntity ?: return InteractionResult.PASS
        val heldItem = player.getItemInHand(hand)

        // Forging: Hammer in Main Hand
        if (heldItem.item is HammerItem) {
            val offhandItem = player.offhandItem
            val itemOnAnvil = blockEntity.inventory.getStackInSlot(0)

            // Case A: Forging Item ALREADY on Anvil (Existing Logic - keep as fallback or for
            // assembly?)
            // User requested: "done with the raw part in the offhand"

            // Case B: Forging Item from Offhand
            if (itemOnAnvil.isEmpty && !offhandItem.isEmpty && offhandItem.item is ToolPartItem) {
                val partItem = offhandItem.item as ToolPartItem
                if (!partItem.isProcessed) {
                    // Process!
                    val processedPart =
                            when (partItem.partType) {
                                ToolPartItem.PartType.HEAD -> RegistryModule.PICKAXE_HEAD.get()
                                ToolPartItem.PartType.AXE_HEAD -> RegistryModule.AXE_HEAD.get()
                                ToolPartItem.PartType.SHOVEL_HEAD ->
                                        RegistryModule.SHOVEL_HEAD.get()
                                ToolPartItem.PartType.SWORD_BLADE ->
                                        RegistryModule.SWORD_BLADE.get()
                                ToolPartItem.PartType.HANDLE -> RegistryModule.TOOL_HANDLE.get()
                                ToolPartItem.PartType.BINDING -> RegistryModule.TOOL_BINDING.get()
                            }

                    // Place Processed Item on Anvil
                    blockEntity.inventory.setStackInSlot(0, ItemStack(processedPart))
                    blockEntity.notifyUpdate()

                    // Consume Raw Part from Offhand
                    if (!player.isCreative) {
                        offhandItem.shrink(1)
                    }

                    // Effects
                    level.playSound(
                            null,
                            pos,
                            SoundEvents.ANVIL_PLACE,
                            SoundSource.BLOCKS,
                            1.0f,
                            1.0f
                    )
                    heldItem.hurtAndBreak(1, player) { p -> p.broadcastBreakEvent(hand) }

                    return InteractionResult.SUCCESS
                }
            }

            // Case A (Existing): Item is already on anvil
            if (!itemOnAnvil.isEmpty && itemOnAnvil.item is ToolPartItem) {
                val partItem = itemOnAnvil.item as ToolPartItem
                if (!partItem.isProcessed) {
                    // Find the processed version
                    val processedPart =
                            when (partItem.partType) {
                                ToolPartItem.PartType.HEAD -> RegistryModule.PICKAXE_HEAD.get()
                                ToolPartItem.PartType.AXE_HEAD -> RegistryModule.AXE_HEAD.get()
                                ToolPartItem.PartType.SHOVEL_HEAD ->
                                        RegistryModule.SHOVEL_HEAD.get()
                                ToolPartItem.PartType.SWORD_BLADE ->
                                        RegistryModule.SWORD_BLADE.get()
                                ToolPartItem.PartType.HANDLE -> RegistryModule.TOOL_HANDLE.get()
                                ToolPartItem.PartType.BINDING -> RegistryModule.TOOL_BINDING.get()
                            }

                    // Transform
                    blockEntity.inventory.setStackInSlot(0, ItemStack(processedPart))
                    blockEntity.notifyUpdate()

                    // Effects
                    level.playSound(
                            null,
                            pos,
                            SoundEvents.ANVIL_PLACE,
                            SoundSource.BLOCKS,
                            1.0f,
                            1.0f
                    )

                    // Damage Hammer
                    heldItem.hurtAndBreak(1, player) { p -> p.broadcastBreakEvent(hand) }

                    return InteractionResult.SUCCESS
                }
            }

            // 2. Assembly from Neighboring Creator Station
            if (itemOnAnvil.isEmpty) {
                for (dir in net.minecraft.core.Direction.values()) {
                    if (dir == net.minecraft.core.Direction.UP ||
                                    dir == net.minecraft.core.Direction.DOWN
                    )
                            continue

                    val neighborPos = pos.relative(dir)
                    val neighborState = level.getBlockState(neighborPos)
                    if (neighborState.block is CreatorStationBlock) {
                        val stationBE =
                                level.getBlockEntity(neighborPos) as?
                                        net.archfoundry.buildersparadise.block.entity.CreatorStationBlockEntity
                                        ?: continue

                        // Check for recipe (Hardcoded for Pickaxe: Head + Handle (+ Binding
                        // optional?))
                        val head = stationBE.inventory.getStackInSlot(0)
                        val handle = stationBE.inventory.getStackInSlot(1)
                        // Binding is slot 2

                        // Pickaxe Recipe: Head + Handle + Binding (if using binding)
                        // For CustomPickaxeItem, let's say Head + Handle + Binding is required for
                        // Tier 2, or just Head + Handle for Tier 1.
                        // Let's assume Head + Binding + Handle for full tool for now.
                        val binding = stationBE.inventory.getStackInSlot(2)

                        if (!head.isEmpty && !handle.isEmpty && !binding.isEmpty) {
                            // Valid Recipe! Assemble!
                            val result = ItemStack(RegistryModule.CUSTOM_PICKAXE_ITEM.get())

                            // Consume Parts
                            stationBE.inventory.setStackInSlot(0, ItemStack.EMPTY)
                            stationBE.inventory.setStackInSlot(1, ItemStack.EMPTY)
                            stationBE.inventory.setStackInSlot(2, ItemStack.EMPTY)
                            stationBE.notifyUpdate()

                            // Place Result
                            blockEntity.inventory.setStackInSlot(0, result)
                            blockEntity.notifyUpdate()

                            level.playSound(
                                    null,
                                    pos,
                                    SoundEvents.ANVIL_USE,
                                    SoundSource.BLOCKS,
                                    1.0f,
                                    1.0f
                            )
                            return InteractionResult.SUCCESS
                        }
                    }
                }
            }
        }

        // Retrieval: Right-click with empty hand (or non-hammer) to take item
        if (heldItem.isEmpty && !blockEntity.inventory.getStackInSlot(0).isEmpty) {
            val extracted = blockEntity.inventory.extractItem(0, 64, false)
            player.setItemInHand(hand, extracted)
            blockEntity.notifyUpdate()
            return InteractionResult.CONSUME
        }

        // Placement handled by Event (swapping vanilla anvil to this) or here if we have a way to
        // place directly?
        // For now, let's assume we mainly swap to this block.
        // But if we are already this block, we might want to swap another item?

        return InteractionResult.PASS
    }

    override fun attack(state: BlockState, level: Level, pos: BlockPos, player: Player) {
        if (level.isClientSide) return

        val blockEntity = level.getBlockEntity(pos) as? ForgeAnvilBlockEntity ?: return
        val heldItem = player.mainHandItem

        if (heldItem.item is HammerItem) {
            // Hammering Logic
            val itemOnAnvil = blockEntity.inventory.getStackInSlot(0)
            if (!itemOnAnvil.isEmpty) {
                // Verify recipe/processing
                // For now: Just spawn particles and sound
                level.playSound(null, pos, SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f)
                // TODO: Actual recipe logic
            }
        }
    }
}
