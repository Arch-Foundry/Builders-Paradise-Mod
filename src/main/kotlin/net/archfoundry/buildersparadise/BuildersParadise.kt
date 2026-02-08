package net.archfoundry.buildersparadise

import net.archfoundry.buildersparadise.client.renderer.CastingBasinRenderer
import net.archfoundry.buildersparadise.client.renderer.CastingTableRenderer
import net.archfoundry.buildersparadise.client.renderer.CreatorStationRenderer
import net.archfoundry.buildersparadise.client.renderer.FaucetRenderer
import net.archfoundry.buildersparadise.client.renderer.ForgeAnvilRenderer
import net.archfoundry.buildersparadise.client.renderer.ForgeControllerRenderer
import net.archfoundry.buildersparadise.client.screen.BuilderStationScreen
import net.archfoundry.buildersparadise.client.screen.SmelteryScreen
import net.archfoundry.buildersparadise.registry.RegistryModule
import net.minecraft.client.gui.screens.MenuScreens
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import thedarkcolour.kotlinforforge.KotlinModLoadingContext

@Mod(BuildersParadise.MOD_ID)
object BuildersParadise {

        const val MOD_ID = "buildersparadise"
        val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

        init {
                val modBus = KotlinModLoadingContext.get().getKEventBus()

                modBus.addListener(::commonSetup)
                modBus.addListener(::clientSetup)
                modBus.addListener(::interModEnqueue)
                modBus.addListener(
                        net.archfoundry.buildersparadise.datagen.DataGenerators::gatherData
                )

                net.minecraftforge.fml.ModLoadingContext.get()
                        .registerConfig(
                                net.minecraftforge.fml.config.ModConfig.Type.SERVER,
                                net.archfoundry.buildersparadise.config.ForgeConfig.SPEC,
                                "buildersparadise-server.toml"
                        )

                RegistryModule.register(modBus)

                // RegistryModule.BLOCKS.register(modBus)
                // RegistryModule.ITEMS.register(modBus)

                MinecraftForge.EVENT_BUS.register(this)
        }

        private fun commonSetup(event: FMLCommonSetupEvent) {
                net.archfoundry.buildersparadise.network.PacketHandler.register()
        }

        private fun interModEnqueue(
                event: net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent
        ) {
                if (net.minecraftforge.fml.ModList.get().isLoaded("theoneprobe")) {
                        net.minecraftforge.fml.InterModComms.sendTo(
                                "theoneprobe",
                                "getTheOneProbe"
                        ) { net.archfoundry.buildersparadise.compat.top.TopPlugin() }
                }
        }

        private fun clientSetup(event: FMLClientSetupEvent) {
                net.minecraft.client.gui.screens.MenuScreens.register(
                        RegistryModule.BUILDER_STATION_MENU.get(),
                        ::BuilderStationScreen
                )
                net.minecraft.client.gui.screens.MenuScreens.register(
                        RegistryModule.SMELTERY_MENU.get(),
                        ::SmelteryScreen
                )
                // Renderer disabled to remove ghost grid
                net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(
                        RegistryModule.CASTING_TABLE_BE.get(),
                        ::CastingTableRenderer
                )
                net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(
                        RegistryModule.CASTING_BASIN_BE.get(),
                        ::CastingBasinRenderer
                )
                net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(
                        RegistryModule.FAUCET_BE.get(),
                        ::FaucetRenderer
                )
                net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(
                        RegistryModule.FORGE_ANVIL_BE.get(),
                        ::ForgeAnvilRenderer
                )
                net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(
                        RegistryModule.CREATOR_STATION_NEW_BE.get(),
                        ::CreatorStationRenderer
                )
                net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(
                        RegistryModule.FORGE_CONTROLLER_BE.get(),
                        ::ForgeControllerRenderer
                )

                // Render Types
                net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(
                        RegistryModule.FORGED_GLASS_BLOCK.get(),
                        net.minecraft.client.renderer.RenderType.translucent()
                )
                net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(
                        RegistryModule.FORGED_TANK_BLOCK.get(),
                        net.minecraft.client.renderer.RenderType.cutoutMipped()
                )
                net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(
                        RegistryModule.MOLTEN_GLASS_BLOCK.get(),
                        net.minecraft.client.renderer.RenderType.translucent()
                )
                net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(
                        RegistryModule.MOLTEN_GLASS_SOURCE.get(),
                        net.minecraft.client.renderer.RenderType.translucent()
                )
                net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(
                        RegistryModule.MOLTEN_GLASS_FLOWING.get(),
                        net.minecraft.client.renderer.RenderType.translucent()
                )
        }
}
