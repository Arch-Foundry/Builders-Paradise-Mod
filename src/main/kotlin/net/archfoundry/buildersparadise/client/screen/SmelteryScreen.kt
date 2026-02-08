package net.archfoundry.buildersparadise.client.screen

import net.archfoundry.buildersparadise.BuildersParadise
import net.archfoundry.buildersparadise.menu.SmelteryMenu
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory

class SmelteryScreen(menu: SmelteryMenu, inv: Inventory, title: Component) :
        AbstractContainerScreen<SmelteryMenu>(menu, inv, title) {

    private val TEXTURE = ResourceLocation(BuildersParadise.MOD_ID, "textures/gui/smeltery.png")

    override fun init() {
        super.init()
        // No extra widgets for now
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderBackground(guiGraphics)
        super.render(guiGraphics, mouseX, mouseY, partialTick)
        renderTooltip(guiGraphics, mouseX, mouseY)
    }

    override fun renderBg(guiGraphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        renderBackgroundFrame(guiGraphics)

        // Progress Arrow
        val progress = menu.progress
        val maxProgress = menu.maxProgress
        if (maxProgress > 0 && progress > 0) {
            val l = progress * 24 / maxProgress
            guiGraphics.blit(TEXTURE, leftPos + 79, topPos + 34, 176, 14, l + 1, 16)
        }

        // Fluid Tank
        val fluidAmount = menu.fluidAmount
        val maxFluid = 4000
        if (fluidAmount > 0) {
            val height = (fluidAmount.toFloat() / maxFluid * 52).toInt() // Tank height ~52px
            val tankX = leftPos + 135
            val tankY = topPos + 17
            val tankH = 52

            guiGraphics.fill(
                    tankX,
                    tankY + tankH - height,
                    tankX + 16,
                    tankY + tankH,
                    0xFFFF4500.toInt()
            )
        }

        // Fuel Flame
        if (menu.maxFuelTime > 0 && menu.fuelTime > 0) {
            val fuelHeight = menu.fuelTime * 14 / menu.maxFuelTime
            // Draw a flame-colored rect from bottom up
            // Flame area: (56, 36) to (56+14, 36+14) approximately?
            // Input is 35, Fuel is 53.
            // Let's position flame relative to fuel slot? or between them?
            // Existing code used: Left+58, Top+55. That's INSIDE the fuel slot (56+2, 53+2).
            // Let's render it nicely in the slot or next to it.
            // Render inside the slot for now, filling up? No, flames usually burn down.
            // Let's burn DOWN (height decreases).
            guiGraphics.fill(
                    leftPos + 56 + 1, // x
                    topPos + 53 + 1 + (16 - fuelHeight), // y + offset
                    leftPos + 56 + 17, // width
                    topPos + 53 + 17, // height
                    0x80FF4500.toInt()
            )
        }
        guiGraphics.drawString(font, "Smeltery", leftPos + 8, topPos + 6, 0x404040, false)
        guiGraphics.drawString(
                font,
                "Fluid: ${menu.fluidAmount}mb",
                leftPos + 100,
                topPos + 20,
                0x404040,
                false
        )
    }

    override fun renderTooltip(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        super.renderTooltip(guiGraphics, mouseX, mouseY)

        // Fuel Tooltip
        if (isHovering(56, 53, 16, 16, mouseX.toDouble(), mouseY.toDouble())) {
            if (menu.fuelTime > 0) {
                val seconds = menu.fuelTime / 20
                val maxSeconds = menu.maxFuelTime / 20
                val pct = if (menu.maxFuelTime > 0) (menu.fuelTime * 100 / menu.maxFuelTime) else 0

                guiGraphics.renderTooltip(
                        font,
                        Component.literal("Fuel: ${seconds}s / ${maxSeconds}s ($pct%)"),
                        mouseX,
                        mouseY
                )
            } else {
                guiGraphics.renderTooltip(font, Component.literal("Fuel: Empty"), mouseX, mouseY)
            }
        }

        // Fluid Tooltip
        if (isHovering(135, 17, 16, 52, mouseX.toDouble(), mouseY.toDouble())) {
            guiGraphics.renderTooltip(
                    font,
                    Component.literal("Fluid: ${menu.fluidAmount} / 4000 mB"),
                    mouseX,
                    mouseY
            )
        }
    }

    private fun renderBackgroundFrame(guiGraphics: GuiGraphics) {
        // Draw standard background (grey box) specific to 176x166
        guiGraphics.fill(
                leftPos,
                topPos,
                leftPos + imageWidth,
                topPos + imageHeight,
                0xFFC6C6C6.toInt()
        )
        // Draw Input Slot (56, 35)
        drawSlot(guiGraphics, 56, 35)
        // Draw Fuel Slot (56, 53)
        drawSlot(guiGraphics, 56, 53)

        // Player Inventory
        for (i in 0..2) {
            for (j in 0..8) {
                drawSlot(guiGraphics, 8 + j * 18, 84 + i * 18)
            }
        }
        // Hotbar
        for (k in 0..8) {
            drawSlot(guiGraphics, 8 + k * 18, 142)
        }
    }

    private fun drawSlot(guiGraphics: GuiGraphics, x: Int, y: Int) {
        guiGraphics.fill(
                leftPos + x,
                topPos + y,
                leftPos + x + 18,
                topPos + y + 18,
                0xFF8B8B8B.toInt()
        )
        guiGraphics.fill(
                leftPos + x,
                topPos + y,
                leftPos + x + 18 - 1,
                topPos + y + 1,
                0xFF373737.toInt()
        )
        guiGraphics.fill(
                leftPos + x,
                topPos + y,
                leftPos + x + 1,
                topPos + y + 18 - 1,
                0xFF373737.toInt()
        )
        guiGraphics.fill(
                leftPos + x + 18 - 1,
                topPos + y,
                leftPos + x + 18,
                topPos + y + 18,
                0xFFFFFFFF.toInt()
        )
        guiGraphics.fill(
                leftPos + x,
                topPos + y + 18 - 1,
                leftPos + x + 18,
                topPos + y + 18,
                0xFFFFFFFF.toInt()
        )
    }
}
