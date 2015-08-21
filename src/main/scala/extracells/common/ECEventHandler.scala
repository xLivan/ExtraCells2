package extracells.common

import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.event.FMLInterModComms
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.gameevent.TickEvent
import net.minecraftforge.event.world.BlockEvent

class ECEventHandler {

  @SubscribeEvent
  def onBlockBreak(e: BlockEvent.BreakEvent): Unit = {
    //TODO: Implement
  }

  @SubscribeEvent
  def onPlayerTick(e: TickEvent.PlayerTickEvent): Unit ={
    //TODO: Implement
  }
}
