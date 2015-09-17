package extracells.common.network

import cpw.mods.fml.common.network.IGuiHandler
import extracells.common.registries.GuiEnum
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.World

class GuiHandler extends IGuiHandler {
  override def getClientGuiElement(ID: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): AnyRef = {

  }

  override def getServerGuiElement(ID: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): AnyRef = ???
}
