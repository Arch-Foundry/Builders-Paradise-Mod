package net.archfoundry.buildersparadise.client.renderer

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.archfoundry.buildersparadise.block.entity.ForgeAnvilBlockEntity
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.level.block.state.properties.BlockStateProperties

class ForgeAnvilRenderer(context: BlockEntityRendererProvider.Context) :
        BlockEntityRenderer<ForgeAnvilBlockEntity> {

    override fun render(
            entity: ForgeAnvilBlockEntity,
            partialTick: Float,
            poseStack: PoseStack,
            bufferSource: MultiBufferSource,
            packedLight: Int,
            packedOverlay: Int
    ) {
        val inventory = entity.inventory
        val stack = inventory.getStackInSlot(0)

        if (!stack.isEmpty) {
            poseStack.pushPose()

            // Center Render on top of Anvil
            poseStack.translate(0.5, 1.0, 0.5)

            // Rotate based on Anvil Facing
            val facing = entity.blockState.getValue(BlockStateProperties.HORIZONTAL_FACING)
            val degrees = -facing.toYRot()
            poseStack.mulPose(Axis.YP.rotationDegrees(degrees))

            // Interaction Rotation (Optional: Rotate item 90 deg so it lies flat)
            // Usually items on anvil lie flat.
            poseStack.mulPose(Axis.XP.rotationDegrees(90f))
            poseStack.scale(0.5f, 0.5f, 0.5f) // Scale item down to fit on anvil surface

            val itemRenderer = Minecraft.getInstance().itemRenderer
            itemRenderer.renderStatic(
                    stack, // The ItemStack to render
                    ItemDisplayContext.FIXED, // Context for item rendering
                    packedLight,
                    packedOverlay,
                    poseStack,
                    bufferSource,
                    entity.level,
                    0 // Seed for random glint offset
            )

            poseStack.popPose()
        }
    }
}
