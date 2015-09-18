package extracells.common.network

import appeng.api.parts.IPartHost
import cpw.mods.fml.common.network.IGuiHandler
import extracells.ExtraCells
import extracells.common.block.TGuiBlock
import extracells.common.item.TGuiItem
import extracells.common.part.TGuiPart
import extracells.common.registries.GuiEnum
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection

object GuiHandler extends IGuiHandler {
  var tempArgs: Option[Array[AnyRef]] = None

  def launchGui(id: Int, player: EntityPlayer, args: Array[AnyRef]): Unit = {
    tempArgs = Option(args)
    player.openGui(ExtraCells, id, null, 0, 0, 0)
  }

  def launchGui(id: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): Unit = {
    player.openGui(ExtraCells, id, world, x, y, z)
  }

  override def getClientGuiElement(sideID: Int, player: EntityPlayer, world: World,
                                   x: Int, y: Int, z: Int): AnyRef = {
    if (player.eq(null))
      return null
    var gui: Option[AnyRef] = None
    /** Handle GUI items. */
    if (world.eq(null))
      gui = Option(player.getCurrentEquippedItem)
        .withFilter(_.getItem.isInstanceOf[TGuiItem])
        .map(stack => stack.getItem.asInstanceOf[TGuiItem].getClientGuiElement(player, stack))

    val block = Option(world.getBlock(x, y, z))
    val side = ForgeDirection.getOrientation(sideID)
    if (gui.isEmpty)
      gui = block.flatMap[AnyRef] {
        case block: TGuiBlock => Option(block.getClientGuiElement(player, world, x, y, z))
        case partHost: IPartHost if side != ForgeDirection.UNKNOWN => partHost.getPart(side) match {
          case part: TGuiPart => Option(part.getClientGuiElement(player))
          case _ => None
        }
        case _ => None
      }

    gui.orNull
  }

  override def getServerGuiElement(sideID: Int, player: EntityPlayer, world: World,
                                   x: Int, y: Int, z: Int): AnyRef = {
    if (player.eq(null))
      return null
    var container: Option[AnyRef] = None
    /** Handle GUI items */
    if (world.eq(null))
      container = Option(player.getCurrentEquippedItem)
        .withFilter(_.getItem.isInstanceOf[TGuiItem])
        .map(stack => stack.getItem.asInstanceOf[TGuiItem].getServerGuiElement(player, stack))

    val block = Option(world.getBlock(x, y, z))
    val side = ForgeDirection.getOrientation(sideID)
    if (container.isEmpty)
      container = block.flatMap[AnyRef] {
        case block: TGuiBlock => Option(block.getServerGuiElement(player, world, x, y, z))
        case partHost: IPartHost if side != ForgeDirection.UNKNOWN => partHost.getPart(side) match {
          case part: TGuiPart => Option(part.getServerGuiElement(player))
          case _ => None
        }
        case _ => None
      }

    container.orNull
  }
}
