package net.archfoundry.buildersparadise.client.renderer

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.archfoundry.buildersparadise.block.entity.CastingTableBlockEntity
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.world.item.ItemDisplayContext

class CastingTableRenderer(context: BlockEntityRendererProvider.Context) :
        BlockEntityRenderer<CastingTableBlockEntity> {

        override fun render(
                entity: CastingTableBlockEntity,
                partialTick: Float,
                poseStack: PoseStack,
                bufferSource: MultiBufferSource,
                packedLight: Int,
                packedOverlay: Int
        ) {
                // Render Item (Slot 1 - Output)
                val itemStack = entity.itemHandler.getStackInSlot(1)
                if (!itemStack.isEmpty) {
                        poseStack.pushPose()
                        // Center on top of block (Standard block height 16px, table might be
                        // lower?)
                        // A table is usually height 14px or 16px. Let's assume surface is at y=1.0
                        // (16px) or
                        // slightly lower e.g. 15/16.
                        // Let's put it at y=1.0 for now.
                        poseStack.translate(0.5, 1.0, 0.5)

                        // Lie flat
                        poseStack.mulPose(Axis.XP.rotationDegrees(90f))
                        poseStack.scale(0.75f, 0.75f, 0.75f)

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

                // Render Cast (Slot 0 - Input)
                val castStack = entity.itemHandler.getStackInSlot(0)
                if (!castStack.isEmpty) {
                        poseStack.pushPose()
                        // Center on top of block
                        // Place slightly lower than Output (e.g. y=1.0 or slightly embedded
                        // 15.5/16)
                        poseStack.translate(
                                0.5,
                                1.001,
                                0.5
                        ) // Slightly above block to avoid z-fighting with block model?
                        // Actually usually table depresses. Let's try 1.0 first. A bit higher than
                        // surface.

                        // Lie flat
                        poseStack.mulPose(Axis.XP.rotationDegrees(90f))
                        poseStack.scale(0.75f, 0.75f, 0.75f)

                        Minecraft.getInstance()
                                .itemRenderer
                                .renderStatic(
                                        castStack,
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
                                net.minecraftforge.client.extensions.common
                                        .IClientFluidTypeExtensions.of(fluidStack.fluid)
                        val sprite =
                                Minecraft.getInstance()
                                        .getTextureAtlas(
                                                net.minecraft.world.inventory.InventoryMenu
                                                        .BLOCK_ATLAS
                                        )
                                        .apply(clientFluid.stillTexture)
                        val color = clientFluid.getTintColor(fluidStack)

                        val r = (color shr 16 and 0xFF) / 255f
                        val g = (color shr 8 and 0xFF) / 255f
                        val b = (color and 0xFF) / 255f
                        val a = (color shr 24 and 0xFF) / 255f

                        val height = 15f / 16f // Fixed height for now, or based on amount
                        val y = height // Surface Y

                        // X/Z bounds - slightly inside the block (assuming 1 pixel rim)
                        val x1 = 1f / 16f
                        val x2 = 15f / 16f
                        val z1 = 1f / 16f
                        val z2 = 15f / 16f

                        val buffer =
                                bufferSource.getBuffer(
                                        net.minecraft.client.renderer.RenderType.translucent()
                                )
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
