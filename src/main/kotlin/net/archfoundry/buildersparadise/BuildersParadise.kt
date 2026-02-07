package net.archfoundry.buildersparadise

import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import thedarkcolour.kotlinforforge.KotlinModLoadingContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Mod(BuildersParadise.MOD_ID)
object BuildersParadise {

    const val MOD_ID = "buildersparadise"
    val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

    init {
        val modBus = KotlinModLoadingContext.get().getKEventBus()

        modBus.addListener(::commonSetup)

        // RegistryModule.BLOCKS.register(modBus)
        // RegistryModule.ITEMS.register(modBus)

        MinecraftForge.EVENT_BUS.register(this)

        LOGGER.info("Builders Paradise (Kotlin) loaded successfully!")
    }

    private fun commonSetup(event: FMLCommonSetupEvent) {

    }
}
