package net.archfoundry.buildersparadise.datagen

import net.archfoundry.buildersparadise.BuildersParadise
import net.archfoundry.buildersparadise.registry.RegistryModule
import net.minecraft.data.PackOutput
import net.minecraftforge.client.model.generators.BlockStateProvider
import net.minecraftforge.common.data.ExistingFileHelper

class ModBlockStateProvider(output: PackOutput, exFileHelper: ExistingFileHelper) :
        BlockStateProvider(output, BuildersParadise.MOD_ID, exFileHelper) {

    override fun registerStatesAndModels() {
        // Forge Bricks
        simpleBlock(RegistryModule.FORGED_BRICKS_BLOCK.get())

        // Forge Glass (Translucent)
        simpleBlock(
                RegistryModule.FORGED_GLASS_BLOCK.get(),
                models().cubeAll("forged_glass", modLoc("block/forged_glass"))
        )

        // Forge Tank (Using specific texture, maybe cutout?)
        simpleBlock(
                RegistryModule.FORGED_TANK_BLOCK.get(),
                models().cubeAll("forged_tank", modLoc("block/forged_tank"))
        )

        // Forge Drain
        simpleBlock(
                RegistryModule.FORGED_DRAIN_BLOCK.get(),
                models().cubeAll("forged_drain", modLoc("block/forged_drain"))
        )

        // Forge Controller (Horizontal)
        val controllerBlock = RegistryModule.FORGE_CONTROLLER_BLOCK.get()
        val controllerModel =
                models().orientable(
                                "forge_controller",
                                modLoc("block/forge_controller_side"),
                                modLoc("block/forge_controller_front"),
                                modLoc("block/forge_controller_top")
                        )
        horizontalBlock(controllerBlock, controllerModel)

        // Fluid Blocks usually don't need complex blockstates here if handled by fluid renderer,
        // but we can register simple states if needed.
        // For now preventing errors by skipping them or simple registration if they have textures.
    }
}
