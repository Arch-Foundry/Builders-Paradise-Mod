package net.archfoundry.buildersparadise

import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Mod(BuildersParadise.MOD_ID)
class BuildersParadise(modBus: IEventBus) {

    companion object {
        const val MOD_ID = "buildersparadise"
        val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)
    }

    init {
        val modBus = FMLJavaModLoadingContext.get().modEventBus

        modBus.addListener<FMLCommonSetupEvent> { event ->
            this.commonSetup(event)
        }

        // RegistryModule.BLOCKS.register(modBus)
        // RegistryModule.ITEMS.register(modBus)

        MinecraftForge.EVENT_BUS.register(this)

        LOGGER.info("Builders Paradise (Kotlin) loaded successfully!")
    }

    private fun commonSetup(event: FMLCommonSetupEvent) {

    }
}
