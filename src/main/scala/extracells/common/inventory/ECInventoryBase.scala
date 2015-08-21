package extracells.common.inventory

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagList

class ECInventoryBase(val name: String, val size: Int, val stackLimit: Int, val updateReceiver: Option[TInevntoryUpdateReceiver] = None) extends IInventory{
  var slots: Array[Option[ItemStack]] = Array.fill(size)(None)

  /**
   * Increases stack size of ItemStack in slot.
   *
   * @param index ID of the slot
   * @param count Amount to add
   * @return The added ItemStack
   */
  def incrStackSize(index: Int, count: Int): ItemStack = {
    val slot = this.slots(index)
    if (slot.isEmpty)
      return null
    val stackLimit = if (getInventoryStackLimit > slot.get.getMaxStackSize)
      slot.get.getMaxStackSize else getInventoryStackLimit
    val slotStack = slot.get
    val addedItems = slotStack.copy()
    addedItems.stackSize = if (slotStack.stackSize + count > stackLimit)
      stackLimit - slotStack.stackSize else count
    slotStack.stackSize += addedItems.stackSize
    this.slots(index) = Option(slotStack)
    markDirty()
    return addedItems
  }
  override def decrStackSize(index: Int, count: Int): ItemStack = {
    val slot = this.slots(index)
    if (slot.isEmpty)
      return null
    if (count < slot.get.stackSize) { // Requested less then contents in slot.
      val stack = slot.get
      val outStack = stack.splitStack(count)
      this.slots(index) = Option(stack)
      markDirty()
      return outStack
    }
    else { // Requested is greater then contents in slot.
      this.slots(index) = None
      markDirty()
      return slot.get
    }
  }

  override def isCustomInventoryName: Boolean = ???
  override def isItemValidForSlot(index: Int, stack: ItemStack): Boolean = ???
  override def isUseableByPlayer(player: EntityPlayer): Boolean = ???

  override def openChest(): Unit = ???
  override def closeChest(): Unit = ???

  override def getInventoryName: String = ???
  override def getSizeInventory: Int = ???
  override def getInventoryStackLimit: Int = ???
  override def getStackInSlot(slotIn: Int): ItemStack = ???
  override def getStackInSlotOnClosing(index: Int): ItemStack = ???

  override def setInventorySlotContents(index: Int, stack: ItemStack): Unit = ???
  override def markDirty(): Unit = ???

  def readFromNBT(tagList: NBTTagList) = ???
  def writeToNBT(tagList: NBTTagList) = ???

}
