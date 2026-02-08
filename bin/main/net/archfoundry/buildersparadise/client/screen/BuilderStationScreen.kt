package net.archfoundry.buildersparadise.client.screen

import net.archfoundry.buildersparadise.menu.BuilderStationMenu
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory

class BuilderStationScreen(menu: BuilderStationMenu, playerInventory: Inventory, title: Component) :
        AbstractContainerScreen<BuilderStationMenu>(menu, playerInventory, title) {

    private var currentLayer = 0
    private val voxels = mutableMapOf<Int, MutableMap<Int, MutableMap<Int, Boolean>>>()
    private val gridSize = 16
    private val cellSize = 8 // Reduced from 10 to fit slot
    private var gridStartX = 0
    private var gridStartY = 0

    override fun init() {
        this.imageWidth = 210
        this.imageHeight = 190 // Reduced height: Grid + Hotbar only
        super.init()
        loadVoxelData()

        // Grid Centered: (210 - 128) / 2 = 41
        gridStartX = leftPos + 41
        gridStartY = topPos + 18 // Below title - Ends at 18 + 128 = 146

        // Buttons: Right side (175)
        val btnX = leftPos + 175
        val btnY = gridStartY

        this.addRenderableWidget(
                net.minecraft.client.gui.components.Button.builder(Component.literal("Up")) {
                            currentLayer++
                        }
                        .bounds(btnX, btnY, 30, 20) // Reduced width to fit
                        .build()
        )

        this.addRenderableWidget(
                net.minecraft.client.gui.components.Button.builder(Component.literal("Dn")) {
                            currentLayer--
                        }
                        .bounds(btnX, btnY + 25, 30, 20)
                        .build()
        )

        this.addRenderableWidget(
                net.minecraft.client.gui.components.Button.builder(Component.literal("Save")) {
                            saveData()
                        }
                        .bounds(btnX, btnY + 50, 30, 20)
                        .build()
        )
    }

    private fun saveData() {
        val nbt = net.minecraft.nbt.CompoundTag()
        voxels.forEach { (x, yMap) ->
            val xTag = net.minecraft.nbt.CompoundTag()
            yMap.forEach { (y, zMap) ->
                val yTag = net.minecraft.nbt.CompoundTag()
                zMap.forEach { (z, state) -> if (state) yTag.putBoolean(z.toString(), true) }
                if (!yTag.isEmpty) xTag.put(y.toString(), yTag)
            }
            if (!xTag.isEmpty) nbt.put(x.toString(), xTag)
        }

        net.archfoundry.buildersparadise.network.PacketHandler.CHANNEL.sendToServer(
                net.archfoundry.buildersparadise.network.packet.UpdateBuilderStationPacket(
                        menu.blockPos,
                        nbt
                )
        )
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        this.renderBackground(guiGraphics)
        super.render(guiGraphics, mouseX, mouseY, partialTick)

        // Render Voxel Grid Frame (Recessed)
        val frameX = gridStartX - 2
        val frameY = gridStartY - 2
        val frameW = gridSize * cellSize + 4
        val frameH = gridSize * cellSize + 4
        drawRecessedBox(guiGraphics, frameX, frameY, frameW, frameH)

        // Render Grid
        for (x in 0 until gridSize) {
            for (z in 0 until gridSize) {
                val screenX = gridStartX + x * cellSize
                val screenY = gridStartY + z * cellSize

                val isSet = voxels[x]?.get(currentLayer)?.get(z) == true
                val color =
                        if (isSet) 0xFF00FF00.toInt() else 0xFF404040.toInt() // Darker empty cells

                guiGraphics.fill(
                        screenX,
                        screenY,
                        screenX + cellSize - 1,
                        screenY + cellSize - 1,
                        color
                )
            }
        }

        // Layer Label
        guiGraphics.drawString(
                font,
                "Layer: $currentLayer",
                gridStartX,
                gridStartY - 10,
                0x404040,
                false
        )
        this.renderTooltip(guiGraphics, mouseX, mouseY)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 0) { // Left Click
            val relX = mouseX - gridStartX
            val relY = mouseY - gridStartY

            if (relX >= 0 && relY >= 0) {
                val gridX = (relX / cellSize).toInt()
                val gridZ = (relY / cellSize).toInt()

                if (gridX in 0 until gridSize && gridZ in 0 until gridSize) {
                    toggleVoxel(gridX, currentLayer, gridZ)
                    return true
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun renderBg(guiGraphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        // Main GUI Background (Beveled)
        drawRaisedBox(guiGraphics, leftPos, topPos, imageWidth, imageHeight)

        // Title
        guiGraphics.drawString(font, title, leftPos + 8, topPos + 6, 0x404040, false)

        // Blueprint Slot (Recessed) - Positioned at X=175, Y=93
        drawRecessedBox(guiGraphics, leftPos + 174, topPos + 92, 18, 18)

        // Render Hotbar Slots Backgrounds
        // Centered at X=24, Y=160
        for (k in 0..8) {
            drawRecessedBox(guiGraphics, leftPos + 23 + k * 18, topPos + 159, 18, 18)
        }
    }

    // Helper: Draw a raised box (standard GUI style)
    private fun drawRaisedBox(guiGraphics: GuiGraphics, x: Int, y: Int, w: Int, h: Int) {
        val cLight = 0xFFFFFFFF.toInt()
        val cDark = 0xFF373737.toInt()
        val cBg = 0xFFC6C6C6.toInt()

        guiGraphics.fill(x, y, x + w, y + h, cBg)
        guiGraphics.fill(x, y, x + w - 1, y + 1, cLight) // Top
        guiGraphics.fill(x, y, x + 1, y + h - 1, cLight) // Left
        guiGraphics.fill(x + w - 1, y, x + w, y + h, cDark) // Right
        guiGraphics.fill(x, y + h - 1, x + w, y + h, cDark) // Bottom
    }

    private fun loadVoxelData() {
        val level = net.minecraft.client.Minecraft.getInstance().level ?: return
        val be = level.getBlockEntity(menu.blockPos)
        if (be is net.archfoundry.buildersparadise.block.entity.BuilderStationBlockEntity) {
            val nbt = be.getVoxelData()
            voxels.clear()

            // Parse NBT: { "0": { "0": { "1": true } } }
            for (xKey in nbt.allKeys) {
                val x = xKey.toIntOrNull() ?: continue
                val yMap = nbt.getCompound(xKey)
                for (yKey in yMap.allKeys) {
                    val y = yKey.toIntOrNull() ?: continue
                    val zMap = yMap.getCompound(yKey)
                    for (zKey in zMap.allKeys) {
                        val z = zKey.toIntOrNull() ?: continue
                        if (zMap.getBoolean(zKey)) {
                            toggleVoxel(x, y, z, true) // Force set to true
                        }
                    }
                }
            }
        }
    }

    // Overload toggleVoxel for explicit setting
    private fun toggleVoxel(x: Int, y: Int, z: Int, forceState: Boolean? = null) {
        val xMap = voxels.computeIfAbsent(x) { mutableMapOf() }
        val yMap = xMap.computeIfAbsent(y) { mutableMapOf() }
        if (forceState != null) {
            yMap[z] = forceState
        } else {
            val currentState = yMap[z] ?: false
            yMap[z] = !currentState
        }
    }

    // Helper: Draw a recessed box (slot/input style)
    private fun drawRecessedBox(guiGraphics: GuiGraphics, x: Int, y: Int, w: Int, h: Int) {
        val cLight = 0xFFFFFFFF.toInt()
        val cDark = 0xFF373737.toInt()
        val cBg = 0xFF8B8B8B.toInt()

        guiGraphics.fill(x, y, x + w, y + h, cBg)
        guiGraphics.fill(x, y, x + w - 1, y + 1, cDark) // Top (Shadow)
        guiGraphics.fill(x, y, x + 1, y + h - 1, cDark) // Left (Shadow)
        guiGraphics.fill(x + w - 1, y, x + w, y + h, cLight) // Right (Highlight)
        guiGraphics.fill(x, y + h - 1, x + w, y + h, cLight) // Bottom (Highlight)
    }
}
