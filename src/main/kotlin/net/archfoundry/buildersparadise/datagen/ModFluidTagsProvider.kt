package net.archfoundry.buildersparadise.datagen

import java.util.concurrent.CompletableFuture
import net.archfoundry.buildersparadise.BuildersParadise
import net.archfoundry.buildersparadise.registry.RegistryModule
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.data.tags.FluidTagsProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraftforge.common.data.ExistingFileHelper
import net.minecraftforge.registries.ForgeRegistries

class ModFluidTagsProvider(
        output: PackOutput,
        lookupProvider: CompletableFuture<HolderLookup.Provider>,
        existingFileHelper: ExistingFileHelper?
) : FluidTagsProvider(output, lookupProvider, BuildersParadise.MOD_ID, existingFileHelper) {

    override fun addTags(provider: HolderLookup.Provider) {
        // Create custom tag for Molten Stone
        // Use "forge:molten_stone" for potential compatibility, or just "forge:stone" if
        // appropriate?
        // Usually "forge:fluids/molten_stone" or "forge:molten_stone"
        // Let's stick with "forge:molten_stone" as a common convention for modded fluids.
        // Also add "minecraft:lava" tag if we want it to behave like lava? No, it's distinct.

        val MOLTEN_STONE_TAG =
                TagKey.create(
                        ForgeRegistries.FLUIDS.registryKey,
                        ResourceLocation("forge", "molten_stone")
                )

        tag(MOLTEN_STONE_TAG)
                .add(RegistryModule.MOLTEN_STONE_SOURCE.get())
                .add(RegistryModule.MOLTEN_STONE_FLOWING.get())

        // Also add to "minecraft:lava" tag?
        // usually custom fluids don't unless they are literally lava.
    }
}
