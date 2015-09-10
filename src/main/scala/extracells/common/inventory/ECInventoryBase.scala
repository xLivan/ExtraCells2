package extracells.common.inventory

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.{NBTTagCompound, NBTTagList}

abstract class ECInventoryBase(val name: String, val size: Int, val stackLimit: Int, receiver: TInventoryUpdateReceiver = null) extends IInventory{
  val updateReceiver: Option[TInventoryUpdateReceiver] = Option(receiver)
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
    addedItems
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
      outStack
    }
    else { // Requested is greater then contents in slot.
      this.slots(index) = None
      markDirty()
      slot.get
    }
  }

  override def isCustomInventoryName: Boolean = false
  override def isItemValidForSlot(index: Int, stack: ItemStack): Boolean
  override def isUseableByPlayer(player: EntityPlayer): Boolean

  //Not required.
  override def openChest(): Unit = {}
  override def closeChest(): Unit = {}

  override def getInventoryName: String = this.name
  override def getSizeInventory: Int = this.size
  override def getInventoryStackLimit: Int = this.stackLimit
  override def getStackInSlot(index: Int): ItemStack = slots(index).orNull
  override def getStackInSlotOnClosing(index: Int): ItemStack = {
    var stack: ItemStack = null
    if (slots(index).isDefined) {
      stack = slots(index).get
      slots(index) = None
    }
    return stack
  }

  override def setInventorySlotContents(index: Int, stack: ItemStack): Unit = {
    if (stack != null && stack.stackSize > getInventoryStackLimit)
      stack.stackSize = getInventoryStackLimit
    this.slots(index) = Option(stack)
    markDirty()
  }
  override def markDirty(): Unit = {
    if (this.updateReceiver.isDefined)
      this.updateReceiver.get.onInventoryChanged()
  }

  def readFromNBT(tagList: NBTTagList): Unit = {
    if(tagList == null)
      return
    for (i <- 0 until tagList.tagCount) {
      val tag = tagList.getCompoundTagAt(i)
      val slot = tag.getByte("Slot") & 255
      if (this.slots.indices.contains(slot))
        this.slots(slot) = Option(ItemStack.loadItemStackFromNBT(tag))
    }
  }
  def writeToNBT(): NBTTagList = {
    val tagList = new NBTTagList
    for (i <- this.slots.indices) {
      if (this.slots(i).isDefined) {
        val tag = new NBTTagCompound
        tag.setByte("Slot", i.toByte)
        this.slots(i).get.writeToNBT(tag)
        tagList.appendTag(tag)
      }
    }
    tagList
  }

}
