package net.archfoundry.buildersparadise.datagen

import net.minecraftforge.data.event.GatherDataEvent

object DataGenerators {
    fun gatherData(event: GatherDataEvent) {
        val generator = event.generator
        val packOutput = generator.packOutput
        val existingFileHelper = event.existingFileHelper
        val lookupProvider = event.lookupProvider

        generator.addProvider(
                event.includeServer(),
                ModFluidTagsProvider(packOutput, lookupProvider, existingFileHelper)
        )
        generator.addProvider(
                event.includeClient(),
                ModBlockStateProvider(packOutput, existingFileHelper)
        )
        generator.addProvider(
                event.includeClient(),
                ModItemModelProvider(packOutput, existingFileHelper)
        )
    }
}
