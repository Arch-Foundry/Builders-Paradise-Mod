package net.archfoundry.buildersparadise.client.tooltip

import net.archfoundry.buildersparadise.BuildersParadise
import net.archfoundry.buildersparadise.registry.RegistryModule
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.client.event.RenderTooltipEvent
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(modid = BuildersParadise.MOD_ID, value = [Dist.CLIENT])
object PonderTooltipHandler {

    // Helper to safely check if item is a Ponder Item
    private fun isPonderItem(item: Item): Boolean {
        return try {
            item == RegistryModule.HAMMER_ITEM.get() ||
                    item == RegistryModule.FORGE_ANVIL_ITEM.get() ||
                    item == RegistryModule.CREATOR_STATION_NEW_ITEM.get()
        } catch (e: Exception) {
            false
        }
    }

    // Helper for W key
    private fun isWKeyDown(): Boolean {
        val window = Minecraft.getInstance().window.window
        return com.mojang.blaze3d.platform.InputConstants.isKeyDown(
                window,
                org.lwjgl.glfw.GLFW.GLFW_KEY_W
        )
    }

    @SubscribeEvent
    fun onItemTooltip(event: ItemTooltipEvent) {
        if (isPonderItem(event.itemStack.item)) {
            val isWDown = isWKeyDown()

            if (isWDown) {
                // We add lines to expand the tooltip so our overlay doesn't cover other things if
                // we rendered inside?
                // Actually, let's keep it simple.
                event.toolTip.add(
                        Component.translatable("gui.buildersparadise.pondering")
                                .withStyle(net.minecraft.ChatFormatting.GOLD)
                )
            } else {
                event.toolTip.add(
                        Component.literal("Hold [W] to Ponder")
                                .withStyle(net.minecraft.ChatFormatting.GRAY)
                )
            }
        }
    }

    @SubscribeEvent
    fun onRenderTooltip(event: RenderTooltipEvent.Pre) {
        val stack = event.itemStack
        if (!isPonderItem(stack.item)) return

        if (!isWKeyDown()) return

        val guiGraphics = event.graphics
        val font = event.font
        val width = 140
        val height = 100

        // Calculate Position: Try to put it above the tooltip, but clamp to screen
        var x = event.x + 10
        var y = event.y - height - 10

        // Clamp to screen
        val screenWidth = Minecraft.getInstance().window.guiScaledWidth
        val screenHeight = Minecraft.getInstance().window.guiScaledHeight

        if (x + width > screenWidth) x = screenWidth - width - 5
        if (y < 5) y = event.y + 20 // If too high, render below cursor

        // Raise Z-Index to ensure it draws over standard tooltip if needed, or just draw.
        // Tooltips usually draw at specific Z.
        // We will just draw.

        // Background
        guiGraphics.fill(x, y, x + width, y + height, 0xFF101010.toInt())
        guiGraphics.renderOutline(x, y, width, height, 0xFFFFD700.toInt())
        guiGraphics.renderOutline(x, y, width, height, 0xFFFFD700.toInt())

        // Header
        guiGraphics.drawCenteredString(
                font,
                "Pondering...",
                x + width / 2,
                y + 5,
                0xFFFFD700.toInt()
        )

        // Content
        val time = System.currentTimeMillis() / 1500
        val step = (time % 3).toInt()

        val text =
                when (step) {
                    0 -> "1. Setup: Anvil + Station"
                    1 -> "2. Forge: Hammer + Part"
                    2 -> "3. Assemble: Parts + Hammer"
                    else -> "..."
                }

        guiGraphics.drawCenteredString(
                font,
                text,
                x + width / 2,
                y + height - 15,
                0xFFFFFFFF.toInt()
        )

        // Icon
        val icon =
                when (step) {
                    0 -> RegistryModule.FORGE_ANVIL_ITEM.get()
                    1 -> RegistryModule.HAMMER_ITEM.get()
                    2 -> RegistryModule.PICKAXE_HEAD.get()
                    else -> RegistryModule.HAMMER_ITEM.get()
                }

        // Scale up icon?
        val pose = guiGraphics.pose()
        pose.pushPose()
        pose.translate((x + width / 2).toDouble(), (y + height / 2).toDouble(), 0.0)
        pose.scale(2.0f, 2.0f, 1.0f)
        pose.translate(-8.0, -8.0, 0.0) // Center 16x16 item
        guiGraphics.renderItem(net.minecraft.world.item.ItemStack(icon), 0, 0)
        pose.popPose()
    }
}
