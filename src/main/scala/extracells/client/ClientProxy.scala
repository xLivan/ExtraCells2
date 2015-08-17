package extracells.client

import cpw.mods.fml.common.eventhandler.SubscribeEvent
import extracells.common.CommonProxy
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.common.MinecraftForge

class ClientProxy extends CommonProxy {
  MinecraftForge.EVENT_BUS.register(this)

  override def registerRenderers() {

  }

  @SubscribeEvent
  def registerTextures(e: TextureStitchEvent.Pre) {

  }

  override def isClient = true
  override def isServer = false
}
