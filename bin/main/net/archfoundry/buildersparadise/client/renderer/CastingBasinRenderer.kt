package net.archfoundry.buildersparadise.client.renderer

import com.mojang.blaze3d.vertex.PoseStack
import net.archfoundry.buildersparadise.block.entity.CastingBasinBlockEntity
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.world.item.ItemDisplayContext

class CastingBasinRenderer(context: BlockEntityRendererProvider.Context) :
        BlockEntityRenderer<CastingBasinBlockEntity> {

    override fun render(
            entity: CastingBasinBlockEntity,
            partialTick: Float,
            poseStack: PoseStack,
            bufferSource: MultiBufferSource,
            packedLight: Int,
            packedOverlay: Int
    ) {
        // Render Item/Block (Slot 0 - Output)
        val itemStack = entity.itemHandler.getStackInSlot(0)
        if (!itemStack.isEmpty) {
            poseStack.pushPose()
            // Center inside basin. Basin usually 16x16x16 roughly.
            poseStack.translate(0.5, 0.5, 0.5)
            // Scale up slightly for block appearance
            poseStack.scale(0.9f, 0.9f, 0.9f)

            Minecraft.getInstance()
                    .itemRenderer
                    .renderStatic(
                            itemStack,
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
        // Check Fluid
        val fluidTank = entity.fluidTank
        if (!fluidTank.isEmpty) {
            val fluidStack = fluidTank.fluid
            val fluidType = fluidStack.fluid.fluidType
            val clientFluid =
                    net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions.of(
                            fluidStack.fluid
                    )
            val sprite =
                    Minecraft.getInstance()
                            .getTextureAtlas(
                                    net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS
                            )
                            .apply(clientFluid.stillTexture)
            val color = clientFluid.getTintColor(fluidStack)

            val r = (color shr 16 and 0xFF) / 255f
            val g = (color shr 8 and 0xFF) / 255f
            val b = (color and 0xFF) / 255f
            val a = (color shr 24 and 0xFF) / 255f

            // Basin height varies by amount
            val amount = fluidTank.fluidAmount
            val capacity = fluidTank.capacity
            val filledRatio = amount.toFloat() / capacity.toFloat()
            val minHeight = 2f / 16f // Bottom of internal basin
            val maxHeight = 15f / 16f // Top of internal basin
            val y = minHeight + (maxHeight - minHeight) * filledRatio

            // X/Z bounds - slightly inside the block (assuming 2 pixel rim for basin)
            val x1 = 2f / 16f
            val x2 = 14f / 16f
            val z1 = 2f / 16f
            val z2 = 14f / 16f

            val buffer =
                    bufferSource.getBuffer(net.minecraft.client.renderer.RenderType.translucent())
            val matrix = poseStack.last().pose()

            // Draw Quad (Top Face)
            buffer.vertex(matrix, x1, y, z1)
                    .color(r, g, b, a)
                    .uv(sprite.u0, sprite.v0)
                    .uv2(packedLight)
                    .normal(0f, 1f, 0f)
                    .endVertex()
            buffer.vertex(matrix, x1, y, z2)
                    .color(r, g, b, a)
                    .uv(sprite.u0, sprite.v1)
                    .uv2(packedLight)
                    .normal(0f, 1f, 0f)
                    .endVertex()
            buffer.vertex(matrix, x2, y, z2)
                    .color(r, g, b, a)
                    .uv(sprite.u1, sprite.v1)
                    .uv2(packedLight)
                    .normal(0f, 1f, 0f)
                    .endVertex()
            buffer.vertex(matrix, x2, y, z1)
                    .color(r, g, b, a)
                    .uv(sprite.u1, sprite.v0)
                    .uv2(packedLight)
                    .normal(0f, 1f, 0f)
                    .endVertex()
        }
    }
}
