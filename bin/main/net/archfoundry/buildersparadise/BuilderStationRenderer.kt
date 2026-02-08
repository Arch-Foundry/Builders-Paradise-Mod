package net.archfoundry.buildersparadise

import com.mojang.blaze3d.vertex.PoseStack
import net.archfoundry.buildersparadise.block.entity.BuilderStationBlockEntity
import net.minecraft.client.renderer.LevelRenderer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider

class BuilderStationRenderer(context: BlockEntityRendererProvider.Context) :
        BlockEntityRenderer<BuilderStationBlockEntity> {

    override fun render(
            blockEntity: BuilderStationBlockEntity,
            partialTick: Float,
            poseStack: PoseStack,
            bufferSource: MultiBufferSource,
            packedLight: Int,
            packedOverlay: Int
    ) {
        val voxelData = blockEntity.getVoxelData()
        if (voxelData.isEmpty) return

        val buffer = bufferSource.getBuffer(RenderType.lines())

        poseStack.pushPose()

        voxelData.allKeys.forEach { xKey ->
            val x = xKey.toIntOrNull()
            if (x != null) {
                val xTag = voxelData.getCompound(xKey)
                xTag.allKeys.forEach { yKey ->
                    val y = yKey.toIntOrNull()
                    if (y != null) {
                        val yTag = xTag.getCompound(yKey)
                        yTag.allKeys.forEach { zKey ->
                            val z = zKey.toIntOrNull()
                            if (z != null) {
                                if (yTag.getBoolean(zKey)) {
                                    // Grid x,y,z -> Rel World x-8, y+1, z-8
                                    val dx = x - 8.0
                                    val dy = y + 1.0
                                    val dz = z - 8.0

                                    poseStack.pushPose()
                                    poseStack.translate(dx, dy, dz)

                                    // Render box wireframe
                                    LevelRenderer.renderLineBox(
                                            poseStack,
                                            buffer,
                                            0.0,
                                            0.0,
                                            0.0,
                                            1.0,
                                            1.0,
                                            1.0,
                                            0.0f,
                                            1.0f,
                                            1.0f,
                                            1.0f
                                    )

                                    poseStack.popPose()
                                }
                            }
                        }
                    }
                }
            }
        }

        poseStack.popPose()
    }

    override fun shouldRenderOffScreen(blockEntity: BuilderStationBlockEntity): Boolean {
        return true
    }
}
