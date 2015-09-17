package extracells.common.network

import cpw.mods.fml.common.network.IGuiHandler
import extracells.common.block.TGuiBlock
import extracells.common.registries.GuiEnum
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.World

class GuiHandler extends IGuiHandler {
  override def getClientGuiElement(ID: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): AnyRef = {
    if (world.eq(null) || player.eq(null))
      return null
    //Handle Block.
    Option(world.getBlock(x, y, z))
      .filter(_.isInstanceOf[TGuiBlock])
      .foreach(_.asInstanceOf[TGuiBlock]
      .getClientGuiElement(player, world, x ,y ,z))

  }

  override def getServerGuiElement(ID: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): AnyRef = {

  }
}
