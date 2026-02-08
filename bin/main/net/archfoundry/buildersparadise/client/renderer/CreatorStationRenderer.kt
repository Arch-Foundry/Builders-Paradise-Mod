package net.archfoundry.buildersparadise.client.renderer

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.archfoundry.buildersparadise.block.entity.CreatorStationBlockEntity
import net.archfoundry.buildersparadise.registry.RegistryModule
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack

class CreatorStationRenderer(context: BlockEntityRendererProvider.Context) :
        BlockEntityRenderer<CreatorStationBlockEntity> {

        override fun render(
                entity: CreatorStationBlockEntity,
                partialTick: Float,
                poseStack: PoseStack,
                bufferSource: MultiBufferSource,
                packedLight: Int,
                packedOverlay: Int
        ) {
                val inventory = entity.inventory
                val time = entity.level?.gameTime ?: 0

                // 1. Find Adjacent Forge Anvil
                var anvilPos: net.minecraft.core.BlockPos? = null
                var anvilDir: net.minecraft.core.Direction? = null

                for (dir in net.minecraft.core.Direction.values()) {
                        if (dir == net.minecraft.core.Direction.UP ||
                                        dir == net.minecraft.core.Direction.DOWN
                        )
                                continue
                        val neighborPos = entity.blockPos.relative(dir)
                        val neighborState = entity.level?.getBlockState(neighborPos)
                        if (neighborState?.block is
                                        net.archfoundry.buildersparadise.block.ForgeAnvilBlock
                        ) {
                                anvilPos = neighborPos
                                anvilDir = dir
                                break
                        }
                }

                // 2. Render Inventory Items (Parts) ON THE ANVIL
                if (anvilPos != null && anvilDir != null) {
                        for (i in 0 until 3) {
                                val stack = inventory.getStackInSlot(i)
                                if (!stack.isEmpty) {
                                        poseStack.pushPose()

                                        // Translate to Anvil Center
                                        poseStack.translate(
                                                anvilDir.stepX.toDouble(),
                                                anvilDir.stepY.toDouble(),
                                                anvilDir.stepZ.toDouble()
                                        )

                                        // Position on Anvil Top
                                        // Layout:
                                        // Head (0): Center
                                        // Handle (1): Offset slightly
                                        // Binding (2): Offset slightly
                                        // OR: Arrange them as if they are laying ready for assembly

                                        poseStack.translate(
                                                0.5,
                                                1.01 + (i * 0.05),
                                                0.5
                                        ) // Stacked slightly

                                        poseStack.mulPose(Axis.XP.rotationDegrees(90f)) // Lay flat
                                        poseStack.mulPose(
                                                Axis.ZP.rotationDegrees(90f * i)
                                        ) // Rotate each layer a bit
                                        if (i == 1)
                                                poseStack.translate(0.2, 0.0, 0.0) // Shift handle

                                        poseStack.scale(0.5f, 0.5f, 0.5f)

                                        Minecraft.getInstance()
                                                .itemRenderer
                                                .renderStatic(
                                                        stack,
                                                        ItemDisplayContext.FIXED,
                                                        packedLight,
                                                        packedOverlay,
                                                        poseStack,
                                                        bufferSource,
                                                        entity.level,
                                                        0
                                                )
                                        poseStack.popPose()
                                }
                        }
                } else {
                        // Fallback: If no anvil, render on station (or don't render?)
                        // User wants them on Anvil. If no anvil, maybe render floating above
                        // station to indicate "Needs Anvil"?
                        // For now, let's keep them on station if no anvil found, so they don't
                        // disappear.
                        for (i in 0 until 3) {
                                val stack = inventory.getStackInSlot(i)
                                if (!stack.isEmpty) {
                                        poseStack.pushPose()
                                        val offsetX = (i - 1) * 0.25
                                        poseStack.translate(0.5 + offsetX, 1.1, 0.5)
                                        poseStack.mulPose(
                                                Axis.YP.rotationDegrees((time + partialTick) * 2f)
                                        )
                                        poseStack.scale(0.3f, 0.3f, 0.3f)
                                        Minecraft.getInstance()
                                                .itemRenderer
                                                .renderStatic(
                                                        stack,
                                                        ItemDisplayContext.GROUND,
                                                        packedLight,
                                                        packedOverlay,
                                                        poseStack,
                                                        bufferSource,
                                                        entity.level,
                                                        0
                                                )
                                        poseStack.popPose()
                                }
                        }
                }
                // 2. Render Ghost Result on Adjacent Anvil
                // Check for recipe matches
                val head = inventory.getStackInSlot(0)
                val handle = inventory.getStackInSlot(1)
                val binding = inventory.getStackInSlot(2)

                if (!head.isEmpty && !handle.isEmpty && !binding.isEmpty) {
                        // Valid Recipe (Hardcoded check again, ideally shared logic)
                        // Determine result
                        val resultStack = ItemStack(RegistryModule.CUSTOM_PICKAXE_ITEM.get())

                        // Find direction to Anvil
                        // The Station doesn't 'point' to the anvil, passing logic checks neighbors.
                        // But visuals need to know WHERE to render.
                        // We can check all neighbors for a ForgeAnvilBlock.

                        for (dir in net.minecraft.core.Direction.values()) {
                                if (dir == net.minecraft.core.Direction.UP ||
                                                dir == net.minecraft.core.Direction.DOWN
                                )
                                        continue

                                val neighborPos = entity.blockPos.relative(dir)
                                val neighborState = entity.level?.getBlockState(neighborPos)
                                if (neighborState?.block is
                                                net.archfoundry.buildersparadise.block.ForgeAnvilBlock
                                ) {
                                        // Render Ghost on THIS neighbor
                                        poseStack.pushPose()

                                        // Translate to Neighbor Center
                                        poseStack.translate(
                                                dir.stepX.toDouble(),
                                                dir.stepY.toDouble(),
                                                dir.stepZ.toDouble()
                                        )

                                        // Anvil Top Offset
                                        poseStack.translate(
                                                0.5,
                                                1.25,
                                                0.5
                                        ) // Higher than item on anvil

                                        // Ghost Effect: Scale up, maybe semi-transparent?
                                        poseStack.scale(0.6f, 0.6f, 0.6f)
                                        poseStack.mulPose(
                                                Axis.YP.rotationDegrees((time + partialTick) * 1f)
                                        )
                                        poseStack.mulPose(
                                                Axis.XP.rotationDegrees(180f)
                                        ) // Point down? Or tool straight up?

                                        Minecraft.getInstance()
                                                .itemRenderer
                                                .renderStatic(
                                                        resultStack,
                                                        ItemDisplayContext.FIXED,
                                                        packedLight, // Full light? Or use
                                                        // packedLight?
                                                        packedOverlay,
                                                        poseStack,
                                                        bufferSource,
                                                        entity.level,
                                                        0
                                                )

                                        poseStack.popPose()
                                        break // Only one anvil supported
                                }
                        }
                }
        }
}
