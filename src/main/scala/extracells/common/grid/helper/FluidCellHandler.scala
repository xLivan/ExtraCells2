package extracells.common.grid.helper

import appeng.api.implementations.tiles.{IMEChest, IChestOrDrive}
import appeng.api.networking.security.PlayerSource
import appeng.api.storage._
import appeng.api.storage.data.{IAEFluidStack, IAEStack}
import extracells.api.storage.IFluidStorageCell
import extracells.client.render.TextureManager
import extracells.common.grid.inventory.FluidCellInventoryHandler
import extracells.common.network.GuiHandler
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.IIcon
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.Fluid

import scala.collection.JavaConverters._
class FluidCellHandler extends ICellHandler {
  type MEInv = IMEInventory[_ <: IAEStack[_ <: IAEStack[_ <: AnyRef]]]
  type MEInvH = IMEInventoryHandler[_ <: IAEStack[_ <: IAEStack[_ <: AnyRef]]]
  //TODO: Add cell idle drain values AND FINISH IMPLEMENTING.
  override def cellIdleDrain(is: ItemStack, handler: MEInv): Double = 0.0

  //Despite the error, its actually correct. Silly intelliJ plugin.
  override def getStatusForCell(is: ItemStack, handler: MEInv): Int = {
    if (handler == null)
      return 0

    val inventory = handler.asInstanceOf[FluidCellInventoryHandler]
    if (inventory.freeBytes == 0)
      return 3
    if (inventory.isFormatted || inventory.usedTypes == inventory.totalTypes)
      return 2
    1
  }

  override def getTopTexture_Dark: IIcon = TextureManager.TERMINAL_FRONT.getTextures()(0)
  override def getTopTexture_Medium: IIcon = TextureManager.TERMINAL_FRONT.getTextures()(1)
  override def getTopTexture_Light: IIcon = TextureManager.TERMINAL_FRONT.getTextures()(2)

  override def getCellInventory(stack: ItemStack, saveProvider: ISaveProvider,
                                channel: StorageChannel): IMEInventoryHandler[IAEFluidStack] = {
    if (channel != StorageChannel.FLUIDS || !stack.getItem.isInstanceOf[IFluidStorageCell])
      return null
    new FluidCellInventoryHandler(stack, saveProvider, stack.getItem.asInstanceOf[IFluidStorageCell].getPreformatted(stack))
  }

  override def openChestGui(player: EntityPlayer, chest: IChestOrDrive,
                            cellHandler: ICellHandler, inv: MEInvH,
                            is: ItemStack, chan: StorageChannel): Unit = {
    if (chan != StorageChannel.FLUIDS)
      return

    val monitorable: Option[IStorageMonitorable] = Option(chest).flatMap {
      case chest: IMEChest => Option(chest.getMonitorable(ForgeDirection.UNKNOWN,
        new PlayerSource(player, chest)))
      case _ => None
    }

    monitorable.foreach(m => GuiHandler.launchGui(GuiHandler.getSideOrID(0),
      player, Array.apply[AnyRef](m.getFluidInventory)))
  }

  override def isCell(stack: ItemStack): Boolean = stack != null &&
    stack.getItem != null && stack.getItem.isInstanceOf[IFluidStorageCell]
}

