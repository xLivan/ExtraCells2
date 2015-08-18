package extracells.common.grid.helper

import appeng.api.implementations.tiles.IChestOrDrive
import appeng.api.storage.data.{IAEStack, IAEFluidStack}
import appeng.api.storage._
import extracells.api.IFluidStorageCell
import extracells.client.render.TextureManager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.IIcon
//TODO: Finish Implementing
class FluidCellHandler extends ICellHandler {
  override def cellIdleDrain(itemStack: ItemStack, imeInventory: IMEInventory[_ <: IAEStack[_]]): Double = {
    return 0D
  }

  override def getStatusForCell(itemStack: ItemStack, imeInventory: IMEInventory[_ <: IAEStack[_]]): Int = {

  }

  override def getTopTexture_Light: IIcon = TextureManager.TERMINAL_FRONT.getTextures()(2)
  override def getTopTexture_Medium: IIcon = TextureManager.TERMINAL_FRONT.getTextures()(1)
  override def getTopTexture_Dark: IIcon = TextureManager.TERMINAL_FRONT.getTextures()(0)

  override def getCellInventory(itemStack: ItemStack, iSaveProvider: ISaveProvider, storageChannel: StorageChannel): IMEInventoryHandler[_ <: IAEStack[_]] = {

  }

  override def openChestGui(entityPlayer: EntityPlayer, iChestOrDrive: IChestOrDrive, iCellHandler: ICellHandler, imeInventoryHandler: IMEInventoryHandler[_ <: IAEStack[_]], itemStack: ItemStack, storageChannel: StorageChannel): Unit = {
    if (storageChannel != StorageChannel.FLUIDS)
      return

  }

  override def isCell(itemStack: ItemStack): Boolean =
    itemStack != null && itemStack.getItem != null && itemStack.getItem.isInstanceOf[IFluidStorageCell]


}

