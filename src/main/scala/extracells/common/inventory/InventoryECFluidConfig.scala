package extracells.common.inventory

import extracells.common.registries.ItemEnum
import extracells.common.util.FluidUtil
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.{NBTTagCompound, NBTTagString, NBTTagList}
import net.minecraftforge.common.util.Constants
import net.minecraftforge.fluids.{FluidRegistry, Fluid}

class InventoryECFluidConfig(name: String, val cellItem: ItemStack, size: Int = 63)
    extends ECInventoryBase(name , size, 1) {
  val tagName = "ec:preformatConfig"
  if (cellItem.hasTagCompound)
    readFromNBT(cellItem.getTagCompound.getTagList(this.tagName, Constants.NBT.TAG_STRING))

  override def isUseableByPlayer(player: EntityPlayer): Boolean = true
  override def isItemValidForSlot(index: Int, stack: ItemStack): Boolean = {
    if (stack == null)
      return false
    /** Special case for FLUIDITEM */
    if (stack.getItem == ItemEnum.FLUIDITEM.getItem) {
      val fluidID = stack.getMetadata
      for (s <- this.slots if s.isDefined)
        if (s.get.getMetadata == fluidID)
          return false
      return false
    }
    /** Basically check if the given itemstack has a fluid in it.*/
    val fluid = FluidUtil.getFilledFluid(stack)
    if (fluid.isEmpty)
      return false
    for (s <- this.slots if s.isDefined)
      if (s.get.getMetadata == fluid.get.getFluidID)
        false
    true
  }

  override def setInventorySlotContents(slot: Int, stack: ItemStack): Unit = {
    if (stack == null) {
      super.setInventorySlotContents(slot, null)
      return
    }
    /** Get the fluid of a container */
    val fluid: Option[Fluid] = if (stack.getItem == ItemEnum.FLUIDITEM.getItem) {
      Option(FluidRegistry.getFluid(stack.getMetadata))
    }
    else {
      if (!isItemValidForSlot(slot, stack))
        None
      else {
        val fluidStack = FluidUtil.getFilledFluid(stack)
        if (fluidStack.isDefined)
          Option(fluidStack.get.getFluid)
        else
          None
      }
    }
    /** And set the slot contents using placeholder item. */
    if (fluid.isEmpty) {
      super.setInventorySlotContents(slot, null)
    }
    super.setInventorySlotContents(slot,
      new ItemStack(ItemEnum.FLUIDITEM.getItem, 1, fluid.get.getID))
  }

  override def markDirty(): Unit = {
    if (!this.cellItem.hasTagCompound)
      this.cellItem.setTagCompound(new NBTTagCompound)
    this.cellItem.getTagCompound.setTag(this.tagName, writeToNBT())
  }

  override def readFromNBT(tagList: NBTTagList): Unit = {
    for (i <- 0 until tagList.tagCount) {
      val fluid = Option(FluidRegistry.getFluid(tagList.getStringTagAt(i)))
      if (fluid.isDefined)
        this.slots(i) = Option(new ItemStack(ItemEnum.FLUIDITEM.getItem, 1, fluid.get.getID))
    }
  }

  override def writeToNBT(): NBTTagList = {
    val tagList = new NBTTagList
    for (s <- this.slots if s.isDefined) {
      val fluid = Option(FluidRegistry.getFluid(s.get.getMetadata))
      if (fluid.isDefined)
        tagList.appendTag(new NBTTagString(fluid.get.getName))
    }
    tagList
  }
}
