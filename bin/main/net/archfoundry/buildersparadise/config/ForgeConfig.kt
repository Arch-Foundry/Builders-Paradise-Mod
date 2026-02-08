package net.archfoundry.buildersparadise.config

import net.minecraftforge.common.ForgeConfigSpec
import org.apache.commons.lang3.tuple.Pair

class ForgeConfig(builder: ForgeConfigSpec.Builder) {
    val minSize: ForgeConfigSpec.IntValue =
            builder.comment(
                            "Minimum size of the internal usage area (width/depth). Default: 1 (for a 3x3 structure)"
                    )
                    .defineInRange("minSize", 1, 1, 64)

    val maxSize: ForgeConfigSpec.IntValue =
            builder.comment(
                            "Maximum size of the internal usage area (width/depth). Default: 7 (for a 9x9 structure)"
                    )
                    .defineInRange("maxSize", 7, 1, 64)

    val maxHeight: ForgeConfigSpec.IntValue =
            builder.comment("Maximum height of the internal usage area. Default: 5")
                    .defineInRange("maxHeight", 5, 1, 256)

    val validFloorBlocks: ForgeConfigSpec.ConfigValue<List<String>> =
            builder.comment("List of blocks valid for the floor of the Forge.")
                    .define("validFloorBlocks", listOf("buildersparadise:forged_bricks"))

    val validWallBlocks: ForgeConfigSpec.ConfigValue<List<String>> =
            builder.comment("List of blocks valid for the walls of the Forge.")
                    .define(
                            "validWallBlocks",
                            listOf(
                                    "buildersparadise:forged_bricks",
                                    "buildersparadise:forged_tank",
                                    "buildersparadise:forged_drain",
                                    "buildersparadise:forged_glass",
                                    "buildersparadise:forge_controller"
                            )
                    )

    companion object {
        val SPEC: ForgeConfigSpec
        val INSTANCE: ForgeConfig

        init {
            val pair: Pair<ForgeConfig, ForgeConfigSpec> =
                    ForgeConfigSpec.Builder().configure(::ForgeConfig)
            INSTANCE = pair.left
            SPEC = pair.right
        }
    }
}
