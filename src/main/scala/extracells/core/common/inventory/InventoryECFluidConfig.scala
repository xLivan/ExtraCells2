package extracells.core.common.inventory

import extracells.core.ExtraCells
import extracells.core.common.registries.ItemEnum
import extracells.core.common.util.FluidUtil
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.{NBTTagCompound, NBTTagList}
import net.minecraftforge.common.util.Constants
import net.minecraftforge.fluids.{Fluid, FluidRegistry}

class InventoryECFluidConfig(name: String, val cellItem: ItemStack, size: Int = 63)
    extends ECInventoryBase(name , size, 1) {
  if (cellItem.hasTagCompound) {
    readFromNBT(cellItem.getTagCompound.getTagList(InventoryECFluidConfig.tagName, Constants.NBT.TAG_COMPOUND))
    this.slots = this.slots.sortWith { (o1, o2) => (o1.isDefined == o2.isDefined) || o1.isDefined }
  }

  override def isUseableByPlayer(player: EntityPlayer): Boolean = true

  override def isItemValidForSlot(index: Int, stackTmp: ItemStack): Boolean = {
    /** Basically preventing one fluid from being set twice. */
    Option(stackTmp).exists {
      case s if s.getItem == ItemEnum.FLUIDPLACEHOLDER.getItem =>
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
    if (!isItemValidForSlot(slot, stack))
      return
    val placeholder = Option(stack).flatMap[Fluid] {
      case st if st.getItem == ItemEnum.FLUIDPLACEHOLDER.getItem =>
        FluidUtil.getFluidFromPlaceholder(st)

      case st => FluidUtil.getFilledFluid(st).map(_.getFluid)
    }.flatMap(f => FluidUtil.getFluidPlaceholder(f))

    super.setInventorySlotContents(slot, placeholder.orNull)
  }

  override def markDirty(): Unit = {
    if (!this.cellItem.hasTagCompound)
      this.cellItem.setTagCompound(new NBTTagCompound)
    this.cellItem.getTagCompound.setTag(InventoryECFluidConfig.tagName, writeToNBT())
  }

  override def readFromNBT(tagList: NBTTagList): Unit = {
    for (i <- 0 until tagList.tagCount) {
      val tag = Option(tagList.getCompoundTagAt(i))

      val fluid: Option[Fluid] = try {
        tag
          .map { t => t.getString("fluid").substring(InventoryECFluidConfig.strPrefix.length) }
          .flatMap(str => Option(FluidRegistry.getFluid(str)))
      } catch {
        case e: IndexOutOfBoundsException =>
          ExtraCells.logger.error("Error loading NBT" + e.toString)
          None
      }

      val slot: Option[Int] = tag
        .map {t => t.getInteger("slot")}
        .filter {_ > 0}
        .map(_ - 1)
        .filter(i => this.slots.indices contains i)

      for {
        s <- slot
        f <- fluid
      } {
        this.slots.update(s, FluidUtil.getFluidPlaceholder(f))
      }
    }
  }


  override def writeToNBT(): NBTTagList = {
    val tagList = new NBTTagList

    for (i <- this.slots.indices) {
      this.slots(i)
        .flatMap {s => FluidUtil.getFluidFromPlaceholder(s)}
        .foreach {f =>
          val tag = new NBTTagCompound
          tag.setInteger("slot", i + 1)
          tag.setString("fluid", InventoryECFluidConfig.strPrefix.concat(f.getName))
          tagList.appendTag(tag)
        }
      }
    tagList
  }
}

object InventoryECFluidConfig {
  val strPrefix = "ec:"
  val tagName = strPrefix.concat("preformatConfig")
}
