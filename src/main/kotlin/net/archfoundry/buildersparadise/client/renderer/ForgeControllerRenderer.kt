package net.archfoundry.buildersparadise.client.renderer

import com.mojang.blaze3d.vertex.PoseStack
import net.archfoundry.buildersparadise.block.ForgeControllerBlock
import net.archfoundry.buildersparadise.block.entity.ForgeControllerBlockEntity
import net.archfoundry.buildersparadise.config.ForgeConfig
import net.minecraft.client.renderer.LevelRenderer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB

class ForgeControllerRenderer(context: BlockEntityRendererProvider.Context) :
        BlockEntityRenderer<ForgeControllerBlockEntity> {

    override fun render(
            be: ForgeControllerBlockEntity,
            partialTick: Float,
            poseStack: PoseStack,
            bufferSource: MultiBufferSource,
            packedLight: Int,
            packedOverlay: Int
    ) {
        val level = be.level ?: return
        val pos = be.blockPos
        val state = be.blockState

        // Only render if player is holding valid items?
        // Or just always render if invalid?
        // Let's render always for now to solve the user's request explicitly.
        // But maybe we can check if they are holding a "Wrench" or "Blueprint" later.
        // For now, if invalid, show Ghost. If valid, show Bounds.

        val builder = bufferSource.getBuffer(RenderType.lines())

        poseStack.pushPose()
        // Translate to 0,0,0 relative to BE is already done by caller?
        // No, renderer is called at BE position.

        if (be.isValid) {
            // Render Green Bounds
            // relative to BE
            val min = be.minPos
            val max = be.maxPos

            // We need to render the box.
            // Coordinates are absolute in world, so we need to offset by -pos.x, -pos.y, -pos.z
            val box =
                    AABB(min)
                            .expandTowards(
                                    max.x - min.x + 1.0,
                                    max.y - min.y + 1.0,
                                    max.z - min.z + 1.0
                            )
                            .move(-pos.x.toDouble(), -pos.y.toDouble(), -pos.z.toDouble())

            LevelRenderer.renderLineBox(poseStack, builder, box, 0f, 1f, 0f, 1f) // Green
        } else {
            // Render Ghost Structure (Red/White) indicating MIN size
            // Default 3x3 internal size (3 width, 3 depth, 1 height?)
            // No, minSize is internal width/depth.
            // minHeight is not strictly defined but usually 1 layer is enough?
            // Controller needs to be on wall.

            val facing = state.getValue(ForgeControllerBlock.FACING)
            val insideDir = facing.opposite
            val leftDir = insideDir.clockWise
            val rightDir = insideDir.counterClockWise

            // Min Size Calculation (internal)
            val minInternal = ForgeConfig.INSTANCE.minSize.get() // e.g. 3
            // Structure includes walls. So external size = minInternal + 2

            // Let's position the ghost so Controller is in the middle of a wall.
            // If min=3, external=5. Center is at index 2 (0,1,2,3,4).
            // Controller is at index 2 of the "front" wall.

            // Wall offset:
            // Controller is AT the wall.
            // Inside starts at relative(insideDir, 1)

            // Center alignment:
            // If size is 3, we have 1 center block, 1 left, 1 right.
            // Total width 3.
            // Controller at center means 1 left, 1 right.

            val halfSize = minInternal / 2
            // Construct box relative to controller

            // Inner Box Logic:
            // Start: 1 block forward (insideDir), then `halfSize` blocks Left
            // End: `minInternal` blocks forward, `halfSize` blocks Right

            // Vectors
            // relative(insideDir, 1) -> Start Depth
            // relative(insideDir, minInternal) -> End Depth (inclusive)

            // relative(leftDir, halfSize) -> Left Edge
            // relative(rightDir, halfSize) -> Right Edge

            // We want the External Box (Walls).
            // Inner Box:
            // x/z space.
            // y space: controller.y to controller.y + 1 (height 1 min?)
            // Actually config doesn't specify minHeight, but code checks loop h in 0..maxHeight.
            // We assume 1 layer is valid (floor + walls + air).

            // Let's render the FLOOR Frame and the WALL Frame.

            // Calculate corners of External Box relative to BE
            // For a 3x3 internal:
            // Depth: 1 (wall) + 3 (air) + 1 (wall) = 5
            // Width: 1 (wall) + 3 (air) + 1 (wall) = 5

            // Controller is in the "Front" wall (index 0 depth?) or "Back" wall?
            // Logic says: `insideDir` is opposite to facing.
            // So facing is OUT.
            // Controller is on the perimeter.

            // Let's draw the bounding box of the expected MINIMUM structure.
            // Depth coverage: Controller(0) to Depth(minInternal+1)
            val depthSize = minInternal + 2
            val widthSize = minInternal + 2

            // Offset for width to center controller
            val widthOffset = widthSize / 2 // integer division? 5/2 = 2.
            // If odd size, centered.

            // Coordinates relative to BE (0,0,0)
            // Forward (Inside): +insideDir * (depthSize - 1)
            // Side 1: +leftDir * widthOffset
            // Side 2: +rightDir * widthOffset
            // Down: -1 (Floor)

            val p1 =
                    BlockPos.ZERO
                            .relative(leftDir, widthOffset)
                            .relative(insideDir, 0)
                            .below(1) // Floor corner near
            val p2 =
                    BlockPos.ZERO
                            .relative(rightDir, widthOffset)
                            .relative(insideDir, depthSize - 1)
                            .above(1) // Top corner far

            // We need min/max of these
            val boxMinX = minOf(p1.x, p2.x).toDouble()
            val boxMinY = minOf(p1.y, p2.y).toDouble()
            val boxMinZ = minOf(p1.z, p2.z).toDouble()
            val boxMaxX = maxOf(p1.x, p2.x).toDouble() + 1.0
            val boxMaxY = maxOf(p1.y, p2.y).toDouble() + 1.0 // Height is 2 (Floor + Wall)
            val boxMaxZ = maxOf(p1.z, p2.z).toDouble() + 1.0

            // Box
            val box = AABB(boxMinX, boxMinY, boxMinZ, boxMaxX, boxMaxY, boxMaxZ)

            LevelRenderer.renderLineBox(poseStack, builder, box, 1f, 0f, 0f, 1f) // Red
        }

        poseStack.popPose()
    }

    override fun shouldRenderOffScreen(be: ForgeControllerBlockEntity): Boolean {
        return true
    }
}
