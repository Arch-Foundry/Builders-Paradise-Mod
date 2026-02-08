package net.archfoundry.buildersparadise.block.entity

import net.archfoundry.buildersparadise.menu.BuilderStationMenu
import net.archfoundry.buildersparadise.registry.RegistryModule
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class BuilderStationBlockEntity(pos: BlockPos, state: BlockState) :
        BlockEntity(RegistryModule.BUILDER_STATION_BE.get(), pos, state), MenuProvider {

    private var voxelData: CompoundTag = CompoundTag()

    // 0 = Invalid, 1 = Valid
    val dataAccess: net.minecraft.world.inventory.ContainerData =
            object : net.minecraft.world.inventory.ContainerData {
                override fun get(index: Int): Int {
                    return if (index == 0) (if (checkStructure()) 1 else 0) else 0
                }

                override fun set(index: Int, value: Int) {}

                override fun getCount(): Int {
                    return 1
                }
            }

    override fun createMenu(
            containerId: Int,
            playerInventory: Inventory,
            player: Player
    ): AbstractContainerMenu {
        return BuilderStationMenu(
                containerId,
                playerInventory,
                inventory,
                dataAccess,
                worldPosition
        )
    }

    override fun getDisplayName(): Component {
        return Component.translatable("block.buildersparadise.builder_station")
    }

    fun setVoxelData(data: CompoundTag) {
        this.voxelData = data
        setChanged()

        // Write to Blueprint Item if present
        val stack = inventory.getItem(0)
        if (!stack.isEmpty && stack.`is`(RegistryModule.BLUEPRINT_ITEM.get())) {
            val itemTag = stack.orCreateTag
            itemTag.put("Voxels", data)
            stack.tag = itemTag
        }

        level?.sendBlockUpdated(worldPosition, blockState, blockState, 3)
    }

    // Validation logic moved to PlaceholderBlock/Anchor
    fun checkStructure(): Boolean {
        // Disabled in Station
        return false
    }

    val inventory =
            object : net.minecraft.world.SimpleContainer(1) {
                override fun setChanged() {
                    super.setChanged()
                    onInventoryChanged()
                }
            }

    private fun onInventoryChanged() {
        if (level == null || level!!.isClientSide) return

        val stack = inventory.getItem(0)
        // If blueprint inserted, load its data
        if (!stack.isEmpty && stack.`is`(RegistryModule.BLUEPRINT_ITEM.get())) {
            val tag = stack.tag
            if (tag != null && tag.contains("Voxels")) {
                val newVoxels = tag.getCompound("Voxels")
                this@BuilderStationBlockEntity.voxelData = newVoxels
                setChanged()
                level?.sendBlockUpdated(worldPosition, blockState, blockState, 3)
            } else {
                // Blank Blueprint -> Write Station Data to Item
                if (!this@BuilderStationBlockEntity.voxelData.isEmpty) {
                    val newTag = stack.orCreateTag
                    newTag.put("Voxels", this@BuilderStationBlockEntity.voxelData.copy())
                    stack.tag = newTag
                    // Ensure the change is saved/synced if needed (Container doesn't know stack NBT
                    // changed unless setItem is called, but stack is reference)
                }
            }
        }
    }

    override fun saveAdditional(tag: CompoundTag) {
        super.saveAdditional(tag)
        tag.put("Voxels", voxelData)
        tag.put("Inventory", inventory.createTag())
    }

    override fun load(tag: CompoundTag) {
        super.load(tag)
        voxelData = tag.getCompound("Voxels")
        if (tag.contains("Inventory")) {
            inventory.fromTag(tag.getList("Inventory", 10))
        }
    }

    override fun getUpdateTag(): CompoundTag {
        val tag = super.getUpdateTag()
        tag.put("Voxels", voxelData)
        return tag
    }

    // ... (rest of methods)

    fun getVoxelData(): CompoundTag {
        return voxelData
    }

    // Helper to drop items on break
    fun drops() {
        net.minecraft.world.Containers.dropContents(level!!, worldPosition, inventory)
    }
}
