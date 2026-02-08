package net.archfoundry.buildersparadise.client.renderer

import com.mojang.blaze3d.vertex.PoseStack
import net.archfoundry.buildersparadise.block.entity.FaucetBlockEntity
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraftforge.common.capabilities.ForgeCapabilities

class FaucetRenderer(context: BlockEntityRendererProvider.Context) :
        BlockEntityRenderer<FaucetBlockEntity> {

        override fun render(
                entity: FaucetBlockEntity,
                partialTick: Float,
                poseStack: PoseStack,
                bufferSource: MultiBufferSource,
                packedLight: Int,
                packedOverlay: Int
        ) {
                if (!entity.active) return

                val level = entity.level ?: return
                val pos = entity.blockPos
                val state = entity.blockState
                val facing = state.getValue(BlockStateProperties.FACING)

                // Try to get fluid from source behind
                val backPos = pos.relative(facing.opposite)
                val sourceBe = level.getBlockEntity(backPos)

                var color = 0
                var sprite: net.minecraft.client.renderer.texture.TextureAtlasSprite? = null

                if (sourceBe != null) {
                        val handler =
                                sourceBe.getCapability(ForgeCapabilities.FLUID_HANDLER, facing)
                                        .orElse(null)
                        if (handler != null && handler.tanks > 0) {
                                val fluidStack = handler.getFluidInTank(0)
                                if (!fluidStack.isEmpty) {
                                        val clientFluid =
                                                net.minecraftforge.client.extensions.common
                                                        .IClientFluidTypeExtensions.of(
                                                        fluidStack.fluid
                                                )
                                        color = clientFluid.getTintColor(fluidStack)
                                        val loc = clientFluid.flowingTexture
                                        sprite =
                                                Minecraft.getInstance()
                                                        .getTextureAtlas(
                                                                net.minecraft.world.inventory
                                                                        .InventoryMenu.BLOCK_ATLAS
                                                        )
                                                        .apply(loc)
                                }
                        }
                }
                if (sprite == null) return
                val renderSprite = sprite!!

                val r = (color shr 16 and 0xFF) / 255f
                val g = (color shr 8 and 0xFF) / 255f
                val b = (color and 0xFF) / 255f
                val a = (color shr 24 and 0xFF) / 255f

                val buffer =
                        bufferSource.getBuffer(
                                net.minecraft.client.renderer.RenderType.translucent()
                        )
                val matrix = poseStack.last().pose()

                // Stream Geometry
                // Centered at 0.5, 0.5 horizontal
                // Y starts at 6/16 (nozzle height) = 0.375
                // Y ends at -10/16 (into block below) approx -0.625. Or just 0 to -16/16.
                // Let's go from Y=0.25 (4/16, bottom of nozzle) down to Y=-0.9 (inside basin)

                // Y starts at 2/16 (nozzle bottom) = 0.125
                // Y ends at -14/16 (into block below)

                val yStart = 2f / 16f

                // Check block below
                val blockBelowState = level.getBlockState(pos.below())
                val blockBelow = blockBelowState.block

                val yEnd =
                        if (blockBelow ==
                                        net.archfoundry.buildersparadise.registry.RegistryModule
                                                .CASTING_BASIN_BLOCK
                                                .get()
                        ) {
                                -12f / 16f // Deep into Basin
                        } else if (!blockBelowState.isAir) {
                                0f // Stop at top of block (Table, etc.)
                        } else {
                                -14f / 16f // Flow through air
                        }

                val uWidth = renderSprite.u1 - renderSprite.u0
                val vHeight = renderSprite.v1 - renderSprite.v0

                // Stream is 4px wide (0.25 of block).
                // Center the U sample: Start at 37.5%, End at 62.5%
                val uStart = renderSprite.u0 + (uWidth * 0.375f)
                val uEnd = renderSprite.u0 + (uWidth * 0.625f)

                // Use full V height for flow
                val vStart = renderSprite.v0
                val vEnd = renderSprite.v1

                val width = 4f / 16f
                val radius = width / 2f // 2 pixels radius

                val x1 = 0.5f - radius
                val x2 = 0.5f + radius
                val z1 = 0.5f - radius
                val z2 = 0.5f + radius

                // Front Face (Z-)
                // Outside
                buffer.vertex(matrix, x1, yStart, z1)
                        .color(r, g, b, a)
                        .uv(uStart, vStart)
                        .uv2(packedLight)
                        .normal(0f, 0f, -1f)
                        .endVertex()
                buffer.vertex(matrix, x1, yEnd, z1)
                        .color(r, g, b, a)
                        .uv(uStart, vEnd)
                        .uv2(packedLight)
                        .normal(0f, 0f, -1f)
                        .endVertex()
                buffer.vertex(matrix, x2, yEnd, z1)
                        .color(r, g, b, a)
                        .uv(uEnd, vEnd)
                        .uv2(packedLight)
                        .normal(0f, 0f, -1f)
                        .endVertex()
                buffer.vertex(matrix, x2, yStart, z1)
                        .color(r, g, b, a)
                        .uv(uEnd, vStart)
                        .uv2(packedLight)
                        .normal(0f, 0f, -1f)
                        .endVertex()
                // Inside
                buffer.vertex(matrix, x2, yStart, z1)
                        .color(r, g, b, a)
                        .uv(uEnd, vStart)
                        .uv2(packedLight)
                        .normal(0f, 0f, 1f)
                        .endVertex()
                buffer.vertex(matrix, x2, yEnd, z1)
                        .color(r, g, b, a)
                        .uv(uEnd, vEnd)
                        .uv2(packedLight)
                        .normal(0f, 0f, 1f)
                        .endVertex()
                buffer.vertex(matrix, x1, yEnd, z1)
                        .color(r, g, b, a)
                        .uv(uStart, vEnd)
                        .uv2(packedLight)
                        .normal(0f, 0f, 1f)
                        .endVertex()
                buffer.vertex(matrix, x1, yStart, z1)
                        .color(r, g, b, a)
                        .uv(uStart, vStart)
                        .uv2(packedLight)
                        .normal(0f, 0f, 1f)
                        .endVertex()

                // Back Face (Z+)
                // Outside
                buffer.vertex(matrix, x2, yStart, z2)
                        .color(r, g, b, a)
                        .uv(uStart, vStart)
                        .uv2(packedLight)
                        .normal(0f, 0f, 1f)
                        .endVertex()
                buffer.vertex(matrix, x2, yEnd, z2)
                        .color(r, g, b, a)
                        .uv(uStart, vEnd)
                        .uv2(packedLight)
                        .normal(0f, 0f, 1f)
                        .endVertex()
                buffer.vertex(matrix, x1, yEnd, z2)
                        .color(r, g, b, a)
                        .uv(uEnd, vEnd)
                        .uv2(packedLight)
                        .normal(0f, 0f, 1f)
                        .endVertex()
                buffer.vertex(matrix, x1, yStart, z2)
                        .color(r, g, b, a)
                        .uv(uEnd, vStart)
                        .uv2(packedLight)
                        .normal(0f, 0f, 1f)
                        .endVertex()
                // Inside
                buffer.vertex(matrix, x1, yStart, z2)
                        .color(r, g, b, a)
                        .uv(uEnd, vStart)
                        .uv2(packedLight)
                        .normal(0f, 0f, -1f)
                        .endVertex()
                buffer.vertex(matrix, x1, yEnd, z2)
                        .color(r, g, b, a)
                        .uv(uEnd, vEnd)
                        .uv2(packedLight)
                        .normal(0f, 0f, -1f)
                        .endVertex()
                buffer.vertex(matrix, x2, yEnd, z2)
                        .color(r, g, b, a)
                        .uv(uStart, vEnd)
                        .uv2(packedLight)
                        .normal(0f, 0f, -1f)
                        .endVertex()
                buffer.vertex(matrix, x2, yStart, z2)
                        .color(r, g, b, a)
                        .uv(uStart, vStart)
                        .uv2(packedLight)
                        .normal(0f, 0f, -1f)
                        .endVertex()

                // Left Face (X-)
                // Outside
                buffer.vertex(matrix, x1, yStart, z2)
                        .color(r, g, b, a)
                        .uv(uStart, vStart)
                        .uv2(packedLight)
                        .normal(-1f, 0f, 0f)
                        .endVertex()
                buffer.vertex(matrix, x1, yEnd, z2)
                        .color(r, g, b, a)
                        .uv(uStart, vEnd)
                        .uv2(packedLight)
                        .normal(-1f, 0f, 0f)
                        .endVertex()
                buffer.vertex(matrix, x1, yEnd, z1)
                        .color(r, g, b, a)
                        .uv(uEnd, vEnd)
                        .uv2(packedLight)
                        .normal(-1f, 0f, 0f)
                        .endVertex()
                buffer.vertex(matrix, x1, yStart, z1)
                        .color(r, g, b, a)
                        .uv(uEnd, vStart)
                        .uv2(packedLight)
                        .normal(-1f, 0f, 0f)
                        .endVertex()
                // Inside
                buffer.vertex(matrix, x1, yStart, z1)
                        .color(r, g, b, a)
                        .uv(uEnd, vStart)
                        .uv2(packedLight)
                        .normal(1f, 0f, 0f)
                        .endVertex()
                buffer.vertex(matrix, x1, yEnd, z1)
                        .color(r, g, b, a)
                        .uv(uEnd, vEnd)
                        .uv2(packedLight)
                        .normal(1f, 0f, 0f)
                        .endVertex()
                buffer.vertex(matrix, x1, yEnd, z2)
                        .color(r, g, b, a)
                        .uv(uStart, vEnd)
                        .uv2(packedLight)
                        .normal(1f, 0f, 0f)
                        .endVertex()
                buffer.vertex(matrix, x1, yStart, z2)
                        .color(r, g, b, a)
                        .uv(uStart, vStart)
                        .uv2(packedLight)
                        .normal(1f, 0f, 0f)
                        .endVertex()

                // Right Face (X+)
                // Outside
                buffer.vertex(matrix, x2, yStart, z1)
                        .color(r, g, b, a)
                        .uv(uStart, vStart)
                        .uv2(packedLight)
                        .normal(1f, 0f, 0f)
                        .endVertex()
                buffer.vertex(matrix, x2, yEnd, z1)
                        .color(r, g, b, a)
                        .uv(uStart, vEnd)
                        .uv2(packedLight)
                        .normal(1f, 0f, 0f)
                        .endVertex()
                buffer.vertex(matrix, x2, yEnd, z2)
                        .color(r, g, b, a)
                        .uv(uEnd, vEnd)
                        .uv2(packedLight)
                        .normal(1f, 0f, 0f)
                        .endVertex()
                buffer.vertex(matrix, x2, yStart, z2)
                        .color(r, g, b, a)
                        .uv(uEnd, vStart)
                        .uv2(packedLight)
                        .normal(1f, 0f, 0f)
                        .endVertex()
                // Inside
                buffer.vertex(matrix, x2, yStart, z2)
                        .color(r, g, b, a)
                        .uv(uEnd, vStart)
                        .uv2(packedLight)
                        .normal(-1f, 0f, 0f)
                        .endVertex()
                buffer.vertex(matrix, x2, yEnd, z2)
                        .color(r, g, b, a)
                        .uv(uEnd, vEnd)
                        .uv2(packedLight)
                        .normal(-1f, 0f, 0f)
                        .endVertex()
                buffer.vertex(matrix, x2, yEnd, z1)
                        .color(r, g, b, a)
                        .uv(uStart, vEnd)
                        .uv2(packedLight)
                        .normal(-1f, 0f, 0f)
                        .endVertex()
                buffer.vertex(matrix, x2, yStart, z1)
                        .color(r, g, b, a)
                        .uv(uStart, vStart)
                        .uv2(packedLight)
                        .normal(-1f, 0f, 0f)
                        .endVertex()
        }
}
