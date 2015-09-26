package extracells.common

import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.event.FMLInterModComms
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.gameevent.TickEvent
import cpw.mods.fml.relauncher.Side
import extracells.common.container.implementations.ContainerFluidStorage
import net.minecraftforge.event.world.BlockEvent

class ECEventHandler {

  @SubscribeEvent
  def onBlockBreak(e: BlockEvent.BreakEvent): Unit = {
    //TODO: Should AE security prevent block break?
  }

  @SubscribeEvent
  def onPlayerTick(e: TickEvent.PlayerTickEvent): Unit ={
    if (e.phase.eq(TickEvent.Phase.START)
        && e.side == Side.SERVER && e.player.ne(null))
      e.player.openContainer match {
        //Energy tick for portable items.
        case container: ContainerFluidStorage => container.energyTick()
      }
  }
}
