package extracells.common.inventory

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack

class UpgradeCardInventory extends IInventory{
  override def decrStackSize(index: Int, count: Int): ItemStack = ???

  override def isCustomInventoryName: Boolean = ???

  override def openChest(): Unit = ???

  override def getSizeInventory: Int = ???

  override def getInventoryStackLimit: Int = ???

  override def closeChest(): Unit = ???

  override def markDirty(): Unit = ???

  override def isItemValidForSlot(index: Int, stack: ItemStack): Boolean = ???

  override def getStackInSlotOnClosing(index: Int): ItemStack = ???

  override def setInventorySlotContents(index: Int, stack: ItemStack): Unit = ???

  override def isUseableByPlayer(player: EntityPlayer): Boolean = ???

  override def getStackInSlot(slotIn: Int): ItemStack = ???

  override def getInventoryName: String = ???
}
