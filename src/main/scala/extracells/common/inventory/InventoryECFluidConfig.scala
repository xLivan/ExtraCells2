package extracells.common.inventory

import extracells.common.registries.ItemEnum
import extracells.common.util.FluidUtil
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.{NBTTagCompound, NBTTagList, NBTTagString}
import net.minecraftforge.common.util.Constants
import net.minecraftforge.fluids.{Fluid, FluidRegistry}

class InventoryECFluidConfig(name: String, val cellItem: ItemStack, size: Int = 63)
    extends ECInventoryBase(name , size, 1) {
  val tagName = "ec:preformatConfig"
  if (cellItem.hasTagCompound)
    readFromNBT(cellItem.getTagCompound.getTagList(this.tagName, Constants.NBT.TAG_STRING))

  override def isUseableByPlayer(player: EntityPlayer): Boolean = true

  override def isItemValidForSlot(index: Int, stackTmp: ItemStack): Boolean = {
    /** Basically preventing one fluid from being set twice. */
    Option(stackTmp).exists {
      case s if s.getItem == ItemEnum.FLUIDITEM.getItem =>
        val fluid = FluidUtil.getFluidFromPlaceholder(s)
        !this.slots.exists(_.flatMap(f => FluidUtil.getFluidFromPlaceholder(f))
          .exists(f2 => fluid.contains(f2)))

      case con if FluidUtil.isFluidContainer(con) =>
        val fluid = FluidUtil.getFilledFluid(con)
        !this.slots.exists(_.flatMap(f => FluidUtil.getFluidFromPlaceholder(f))
          .exists(f2 => fluid.contains(f2)))

      case _ => false
    }
  }

  override def setInventorySlotContents(slot: Int, stack: ItemStack): Unit = {
    Option(stack).map {
      case st if st.getItem == ItemEnum.FLUIDITEM.getItem =>
        FluidUtil.getFluidFromPlaceholder(st)

      case st if FluidUtil.is
    }



    if (stack == null) {
      super.setInventorySlotContents(slot, null)
      return
    }
    /** Get the fluid of a container */
    val fluid: Option[Fluid] = if (stack.getItem == ItemEnum.FLUIDITEM.getItem) {
      FluidUtil.getFluidFromPlaceholder(stack)
    }
    else {
      if (!isItemValidForSlot(slot, stack))
        None
      else {
        FluidUtil.getFilledFluid(stack).map(_.getFluid)
      }
    }
    /** And set the slot contents using placeholder item. */
    super.setInventorySlotContents(slot,
      FluidUtil.getFluidPlaceholder(fluid.orNull)
        .orNull)
  }

  override def markDirty(): Unit = {
    if (!this.cellItem.hasTagCompound)
      this.cellItem.setTagCompound(new NBTTagCompound)
    this.cellItem.getTagCompound.setTag(this.tagName, writeToNBT())
  }

  override def readFromNBT(tagList: NBTTagList): Unit = {
    for (i <- 0 until tagList.tagCount) {
      val fluid = Option(FluidRegistry.getFluid(tagList.getStringTagAt(i)))
      fluid.foreach(f => this.slots(i) = FluidUtil.getFluidPlaceholder(f))
    }
  }

  override def writeToNBT(): NBTTagList = {
    val tagList = new NBTTagList

    for (slot <- this.slots) {
      slot.withFilter(_.hasTagCompound)
        .flatMap(s => FluidUtil.getFluidFromPlaceholder(s))
        .foreach(f => tagList.appendTag(new NBTTagString(f.getName)))
      }
    tagList
  }
}
