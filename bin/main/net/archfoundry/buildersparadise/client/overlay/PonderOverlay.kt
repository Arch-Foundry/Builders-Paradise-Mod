package net.archfoundry.buildersparadise.client.overlay

import net.archfoundry.buildersparadise.BuildersParadise
import net.minecraftforge.client.event.RenderGuiOverlayEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(
        modid = BuildersParadise.MOD_ID,
        value = [net.minecraftforge.api.distmarker.Dist.CLIENT]
)
object PonderOverlay {

    @SubscribeEvent
    fun onRenderGui(event: RenderGuiOverlayEvent.Post) {
        // Disabled per user request (Ponder UI is now in Inventory Tooltip only)
        return
    }
}
