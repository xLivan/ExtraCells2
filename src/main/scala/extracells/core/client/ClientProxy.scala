package extracells.core.client

import cpw.mods.fml.common.eventhandler.SubscribeEvent
import extracells.core.client.render.TextureEnum
import extracells.core.client.render.item.ItemRendererFluidPlaceholder
import extracells.core.common.CommonProxy
import extracells.core.common.registries.ItemEnum
import net.minecraftforge.client.MinecraftForgeClient
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.common.MinecraftForge

class ClientProxy extends CommonProxy {
  MinecraftForge.EVENT_BUS.register(this)

  override def registerRenderers() {
    MinecraftForgeClient.registerItemRenderer(ItemEnum.FLUIDPLACEHOLDER.getItem, new ItemRendererFluidPlaceholder)
  }

  @SubscribeEvent
  def registerTextures(e: TextureStitchEvent.Pre) {
    TextureEnum.values.foreach(_.registerTexture(e.map))
  }

  override def isClient = true
  override def isServer = false
}
