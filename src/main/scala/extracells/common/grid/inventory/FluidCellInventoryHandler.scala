package extracells.common.grid.inventory

import net.minecraftforge.common.util.Constants

import scala.collection.mutable
import scala.collection.mutable.Stack
import java.util.{ArrayList => JavaArrayList, List => JavaList}

import appeng.api.config.{AccessRestriction, Actionable}
import appeng.api.networking.security.BaseActionSource
import appeng.api.storage.data.{IAEFluidStack, IItemList}
import appeng.api.storage.{ISaveProvider, IMEInventoryHandler, StorageChannel}
import extracells.api.{IFluidStorageCell, ECApi, IHandlerFluidStorage}
import extracells.common.container.implementations.ContainerFluidStorage
import net.minecraft.item.ItemStack
import net.minecraft.nbt.{NBTTagList, NBTTagCompound}
import net.minecraftforge.fluids.{Fluid, FluidStack}

/**
 * Fluid Cell Handler
 *
 * Tag Version 1
 * Note: Except for loading NBT data, all operations to stored fluids must go through
 * the FluidList wrapper. This should ensure consistency between NBT and actual data.
 *
 * @param storageStack
 * @param saveProvider
 */

class FluidCellInventoryHandler(storageStack: ItemStack, val saveProvider: ISaveProvider) extends IMEInventoryHandler[IAEFluidStack] with IHandlerFluidStorage{
  final private val tagVersionKey = "ec:tagVersion"
  final private val tagVersion = 1
  final private val tagStoredFluids = "ec:storedFluids"

  private val stackTag: NBTTagCompound = if (storageStack.hasTagCompound) storageStack.getTagCompound
    else {
      val tag: NBTTagCompound = new NBTTagCompound
      storageStack.setTagCompound(tag)
      tag
    }
  protected var storedFluids: FluidList = _
  private var prioritizedFluids: JavaList[Fluid] = new JavaArrayList[Fluid]()
  val bytesPerType: Int = storageStack.getItem.asInstanceOf[IFluidStorageCell].getBytesPerType(storageStack)
  val totalTypes: Int = storageStack.getItem.asInstanceOf[IFluidStorageCell].getMaxTypes(storageStack)
  val totalBytes: Int = storageStack.getItem.asInstanceOf[IFluidStorageCell].getMaxBytes(storageStack)
  private var containers: JavaList[ContainerFluidStorage] = new JavaArrayList[ContainerFluidStorage]()

  loadNBTTag()

  def this(storageStack: ItemStack, saveProvider: ISaveProvider, filter: JavaList[Fluid]) {
    this(storageStack, saveProvider)
    if (filter != null)
      this.prioritizedFluids = filter
  }


  override def injectItems(input: IAEFluidStack, mode: Actionable, source: BaseActionSource): IAEFluidStack = {
    lazy val fluidIndex = this.storedFluids.search(input.getFluid)
    var requiredBytes: Int = 0

    //Check if can inject
    if (input == null || !isAllowedByFormat(input.getFluid))
      return input
    if (fluidIndex == -1) {
      //Out of types, can't accept!
      if (this.storedFluids.length >= this.totalTypes)
        return input
      requiredBytes += bytesPerType
    }
    requiredBytes += input.getStackSize.toInt

    if (requiredBytes <= this.freeBytes) { //If required space fits in free space
      if (fluidIndex == -1) //If type is not already in storage
        if (mode == Actionable.MODULATE)
          this.storedFluids.add(input.getFluidStack)
      else { //If type is already in storage
        if (mode == Actionable.MODULATE) {
          val fluidStack = this.storedFluids.get(fluidIndex)
          fluidStack.amount += input.getStackSize.toInt
          this.storedFluids.set(fluidIndex, fluidStack)
        }
      }
      return null
    }
    else { //If it will not fit
      var amountInjected;
      if (fluidIndex == -1) { //If type is not already in storage
        amountInjected = this.freeBytes - this.bytesPerType
        if (mode == Actionable.MODULATE) {
          val fluidStack = new FluidStack(input.getFluid, amountInjected)
          this.storedFluids.add(fluidStack)
        }
      }
      else { //If type is already in storage
        amountInjected = this.freeBytes
        if (mode == Actionable.MODULATE) {
          val fluidStack = this.storedFluids.get(fluidIndex)
          fluidStack.amount += amountInjected
          this.storedFluids.set(fluidIndex, fluidStack)
        }
      }
      input.setStackSize(input.getStackSize - amountInjected)
      return input
    }


  }
  override def extractItems(stackType: IAEFluidStack, actionable: Actionable, baseActionSource: BaseActionSource): IAEFluidStack = ???
  override def getAvailableItems(iItemList: IItemList[IAEFluidStack]): IItemList[IAEFluidStack] = ???

  def isAllowedByFormat(input: Fluid): Boolean =
    !isFormatted || this.prioritizedFluids.contains(input)
  override def isPrioritized(input: IAEFluidStack): Boolean = input != null &&
    this.prioritizedFluids.contains(input.getFluid)
  override def isFormatted: Boolean = {
    if (this.prioritizedFluids.isEmpty)
      return false
    for (fluid: Fluid <- this.prioritizedFluids if fluid != null)
      return true
  }
  override def canAccept(input: IAEFluidStack): Boolean = {
    if (input == null)
      return false
    if (!ECApi.instance.canStoreFluid(input.getFluid))
      return false
    for (stack: FluidStack <- this.storedFluids)
      if (stack == null || stack.getFluid == input.getFluid)
        return this.isAllowedByFormat(input.getFluid)
    false
  }
  override def validForPass(i: Int): Boolean = ???

  override def getSlot: Int = ???
  override def getPriority: Int = ???
  override def getAccess: AccessRestriction = ???
  override def getChannel: StorageChannel = ???

  def freeBytes: Int = {
    var i: Int = 0
    for (stack: FluidStack <- this.storedFluids if (stack != null))
      i += stack.amount
    this.totalBytes - i
  }
  override def usedBytes(): Int = this.totalBytes - this.freeBytes
  override def usedTypes(): Int = {
    var count: Int = 0
    for (stack: FluidStack <- this.storedFluids if (stack != null))
      count += 1
    count
  }

  protected def updateStoredFluid(index: Int, fluidStack: FluidStack): Unit = {
      if(fluidStack != null && fluidStack.getFluidID > 0 && fluidStack.amount > 0)
        this.storedFluids.set(index, fluidStack)
      else
        this.storedFluids.remove(index)
  }

  private def loadNBTTag(): Unit = {
    this.stackTag.getInteger(this.tagVersionKey) match {
      //Original EC2 format, maintained for backwards compatibility.
      case 0 => this.storedFluids = new FluidList(new NBTTagList)
        for (slot: Int <- 0 until this.totalTypes) {
          val fluidStack = FluidStack.loadFluidStackFromNBT(this.stackTag.getCompoundTag("Fluid#" + slot))
          if (fluidStack != null)
            this.storedFluids.add(fluidStack)
        }
        updateNBTTagVersion()
      //Current format
      case 1 =>
        this.storedFluids = new FluidList(this.stackTag.getTagList(this.tagStoredFluids,Constants.NBT.TAG_COMPOUND))
        this.stackTag.setTag(this.tagStoredFluids, this.storedFluids.fluidsTag)
        //Because 63 types is the maximum AE allows.
        //Also, larger then maxTypes is allowed for maybe map makers to preload fluids via NBT.
        for (index <- 0 until this.storedFluids.fluidsTag.tagCount if index < 63) {
          val fluidStack = FluidStack.loadFluidStackFromNBT(this.storedFluids.fluidsTag.getCompoundTagAt(index))
          if (fluidStack != null)
            this.storedFluids.add(fluidStack)
          else // Remove invalid fluid from tag.
            this.storedFluids.fluidsTag.removeTag(index)
        }
    }
  }

  private def updateNBTTagVersion(): Unit = {
    this.stackTag.setInteger(this.tagStoredFluids, this.tagVersion)
    this.stackTag.setTag(this.tagStoredFluids, this.storedFluids.fluidsTag)
    for (fluidStack: FluidStack <- this.storedFluids) {
      val tag = new NBTTagCompound()
      fluidStack.writeToNBT(tag)
      this.storedFluids.fluidsTag.appendTag(tag)
    }
  }

  /**
   * Basically a wrapper around ArrayList so that operations
   * will update the NBT tag at the same time.
   *
   * @param fluidsTag
   */
  private class FluidList(val fluidsTag: NBTTagList) {
    val fluidsList = new JavaArrayList[FluidStack]()

    def add(stack: FluidStack): Unit = add(stack, true)
    def add(stack: FluidStack, updateNBT: Boolean): Unit = {
      fluidsList.add(stack)
      if (updateNBT) {
        val tag = new NBTTagCompound
        stack.writeToNBT(tag)
        fluidsTag.appendTag(tag)
      }
    }

    def remove(index: Int): Unit = {
      fluidsList.remove(index)
      fluidsTag.removeTag(index)
    }

    def set(index: Int, stack: FluidStack): Unit = {
      fluidsList.set(index, stack)
      val tag = new NBTTagCompound
      stack.writeToNBT(tag)
      fluidsTag.func_150304_a(index, tag)
    }

    /** Used to get a FluidStack for index*/
    def get(index: Int): FluidStack = {
      fluidsList.get(index)
    }

    def length: Int = fluidsList.size()

    /**
     * Used to search for a fluid entry
     *
     * @param fluid Fluid to search for
     * @return Int Index of entry
     */
    def search(fluid: Fluid): Int = {
      for (stack: FluidStack <- fluidsList if stack.getFluid == fluid)
        return fluidsList.indexOf(stack)
      return -1
    }
  }
}
