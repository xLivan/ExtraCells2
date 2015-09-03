package extracells.common.grid.helper

import appeng.api.implementations.tiles.IChestOrDrive
import appeng.api.storage.data.{IAEStack, IAEFluidStack}
import appeng.api.storage._
import extracells.api.storage.{IHandlerFluidStorage, IFluidStorageCell}
import extracells.client.render.TextureManager
import extracells.common.grid.inventory.FluidCellInventoryHandler
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.IIcon

class FluidCellHandler extends ICellHandler {
  //TODO: Add cell idle drain values
  override def cellIdleDrain(is: ItemStack, handler: IMEInventory[_ <: IAEStack[_]]): Double = 0D

  override def getStatusForCell(is: ItemStack, handler: IMEInventory[_ <: IAEStack[_]]): Int = {
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

  override def getCellInventory(stack: ItemStack, saveProvider: ISaveProvider, channel: StorageChannel): IMEInventoryHandler[_ <: IAEStack[_]] = {
    if (channel != StorageChannel.FLUIDS || !stack.isInstanceOf[IFluidStorageCell])
      return null
    new FluidCellInventoryHandler(stack, saveProvider, stack.getItem.asInstanceOf[IFluidStorageCell].getPreformatted(stack))
  }

  override def openChestGui(player: EntityPlayer, chest: IChestOrDrive, cellHandler: ICellHandler, inv: IMEInventoryHandler[_ <: IAEStack[_]], is: ItemStack, chan: StorageChannel): Unit = ???

  override def isCell(stack: ItemStack): Boolean = stack != null &&
    stack.getItem != null && stack.getItem.isInstanceOf[IFluidStorageCell]
}

