package net.archfoundry.buildersparadise.datagen

import net.archfoundry.buildersparadise.BuildersParadise
import net.archfoundry.buildersparadise.registry.RegistryModule
import net.minecraft.data.PackOutput
import net.minecraft.world.item.Item
import net.minecraftforge.client.model.generators.ItemModelProvider
import net.minecraftforge.common.data.ExistingFileHelper
import net.minecraftforge.registries.RegistryObject

class ModItemModelProvider(output: PackOutput, existingFileHelper: ExistingFileHelper) :
        ItemModelProvider(output, BuildersParadise.MOD_ID, existingFileHelper) {

    override fun registerModels() {
        // Block Items
        simpleBlockItem(RegistryModule.FORGED_BRICKS_BLOCK.get().asItem())
        buttonItem(
                RegistryModule.FORGED_GLASS_BLOCK.get().asItem(),
                modLoc("block/forged_glass")
        ) // Using flat valid texture
        buttonItem(RegistryModule.FORGED_TANK_BLOCK.get().asItem(), modLoc("block/forged_tank"))
        buttonItem(RegistryModule.FORGED_DRAIN_BLOCK.get().asItem(), modLoc("block/forged_drain"))

        // Controller
        withExistingParent(
                RegistryModule.FORGE_CONTROLLER_ITEM.getId().path,
                modLoc("block/forge_controller")
        )

        // Buckets
        simpleItem(RegistryModule.MOLTEN_STONE_BUCKET)
        simpleItem(RegistryModule.MOLTEN_GLASS_BUCKET)
    }

    private fun simpleItem(item: RegistryObject<Item>) {
        withExistingParent(item.id.path, mcLoc("item/generated"))
                .texture("layer0", modLoc("item/" + item.id.path))
    }

    private fun simpleBlockItem(item: Item) {
        withExistingParent(
                net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(item)!!.path,
                modLoc(
                        "block/" +
                                net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(item)!!
                                        .path
                )
        )
    }

    private fun buttonItem(item: Item, texture: net.minecraft.resources.ResourceLocation) {
        // Just linking to the block model if it's a simple block
        withExistingParent(
                net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(item)!!.path,
                modLoc(
                        "block/" +
                                net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(item)!!
                                        .path
                )
        )
    }
}
