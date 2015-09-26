package extracells.common.network

import appeng.api.parts.IPartHost
import appeng.api.storage.IMEMonitor
import appeng.api.storage.data.IAEFluidStack
import cpw.mods.fml.common.network.IGuiHandler
import extracells.ExtraCells
import extracells.client.gui.GuiFluidStorage
import extracells.common.block.TGuiBlock
import extracells.common.container.implementations.ContainerFluidStorage
import extracells.common.item.TGuiItem
import extracells.common.part.{PartECBase, TGuiPart}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection

object GuiHandler extends IGuiHandler {
  val GUI_ITEM_ID = -1
  var tempArgs: Option[Array[AnyRef]] = None

  def launchGui(id: Int, player: EntityPlayer, args: Array[AnyRef]): Unit = {
    tempArgs = Option(args)
    player.openGui(ExtraCells, id, null, 0, 0, 0)
  }

  def launchGui(id: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): Unit = {
    player.openGui(ExtraCells, id, world, x, y, z)
  }

  /**
   * Gets the GUI SideID from a given gui ID.
   * @param id GUI ID
   * @return
   */
  def getSideOrID(id: Int): Int = id + 6

  /**
   * Gets the GUI SideID from a given part
   * @param part Part.
   * @return
   */
  def getSideOrID(part: PartECBase): Int = part.getSide.ordinal()

  override def getClientGuiElement(sideID: Int, player: EntityPlayer, world: World,
                                   x: Int, y: Int, z: Int): AnyRef = {
    if (player.eq(null))
      return null
    var gui: Option[AnyRef] = None

    /** Handle GUI id's */
    if (world.eq(null) && gui.isEmpty && sideID >= 6)
      gui = sideID - 6 match {

        case 0 => Option(new GuiFluidStorage(player, "extracells.part.fluid.terminal.name"))
        case _ => None
      }

    /** Handle GUI items. */
    if (world.eq(null) && sideID == GUI_ITEM_ID)
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

    /** Handle GUI id's */
    if (world.eq(null) && container.isEmpty && sideID >= 6)
      container = sideID - 6 match {
        case 0 => val mon = tempArgs.map(_(0).asInstanceOf[IMEMonitor[IAEFluidStack]])
          Option(new ContainerFluidStorage(mon.get, player))
        // 1&3 are placeholders TODO: Portable terminals
        case _ => None
      }

    /** Handle GUI items */
    if (world.eq(null) && sideID == GUI_ITEM_ID)
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
