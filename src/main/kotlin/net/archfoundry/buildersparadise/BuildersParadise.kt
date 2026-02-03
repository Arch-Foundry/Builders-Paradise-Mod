package net.archfoundry.buildersparadise

import net.minecraftforge.fml.common.Mod
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Mod(BuildersParadise.MOD_ID)
class BuildersParadise {

    companion object {
        const val MOD_ID = "buildersparadise"
        val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)
    }

    init {
        LOGGER.info("Builders Paradise (Kotlin) loaded successfully!")
    }
}
