package extracells.common.grid.inventory

import java.util.{ArrayList => JavaArrayList, List => JavaList}

import appeng.api.config.{AccessRestriction, Actionable}
import appeng.api.networking.security.BaseActionSource
import appeng.api.storage.data.{IAEFluidStack, IItemList}
import appeng.api.storage.{IMEInventoryHandler, ISaveProvider, StorageChannel}
import extracells.api.{ECApi, IFluidStorageCell, IHandlerFluidStorage}
import extracells.common.container.implementations.ContainerFluidStorage
import net.minecraft.item.ItemStack
import net.minecraft.nbt.{NBTTagCompound, NBTTagList}
import net.minecraftforge.common.util.Constants
import net.minecraftforge.fluids.{Fluid, FluidStack}

import scala.collection.mutable

/**
 * Fluid Cell Handler
 *
 * Tag Version 1
 * Note: Except for loading NBT data, all operations to stored fluids must go through
 * the FluidSet wrapper. This should ensure consistency between NBT and actual data.
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
  protected var storedFluids: FluidSet = _
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
    for (stack: FluidStack <- this.storedFluids if stack != null)
      i += stack.amount
    this.totalBytes - i
  }
  override def usedBytes(): Int = this.totalBytes - this.freeBytes
  override def usedTypes(): Int = {
    var count: Int = 0
    for (stack: FluidStack <- this.storedFluids if stack != null)
      count += 1
    count
  }

  private def loadNBTTag(): Unit = {
    this.stackTag.getInteger(this.tagVersionKey) match {
      //Original EC2 format, maintained for backwards compatibility.
      case 0 => this.storedFluids = new FluidSet(new NBTTagList)
        for (slot: Int <- 0 until this.totalTypes) {
          val tagKey = "Fluid#" + slot
          val fluidStack = FluidStack.loadFluidStackFromNBT(this.stackTag.getCompoundTag(tagKey))
          if (fluidStack != null)
            this.storedFluids.updateFluid(fluidStack)
          this.stackTag.removeTag(tagKey)
        }
        updateNBTTagVersion()
      //Current format
      case 1 =>
        this.storedFluids = new FluidSet(this.stackTag.getTagList(this.tagStoredFluids,Constants.NBT.TAG_COMPOUND))
        this.stackTag.setTag(this.tagStoredFluids, this.storedFluids.fluidsTag)

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
  private class FluidSet(val fluidsTag: NBTTagList) {
    val fluidsMap = new mutable.HashMap[Fluid, (FluidStack, NBTTagCompound)]()
    for (i <- 0 until fluidsTag.tagCount()) {
      val tag = fluidsTag.getCompoundTagAt(i)
      val fluidStack = FluidStack.loadFluidStackFromNBT(tag)
      if (fluidStack != null)
        fluidsMap.put(fluidStack.getFluid, (fluidStack, tag))
      else
        fluidsTag.removeTag(i)
    }

    def updateFluid(stack: FluidStack): Unit = {
      var tag: NBTTagCompound = _
      if (stack == null)
        return
      //If doesn't already exist, create NBT Tag
      if (!fluidsMap.contains(stack.getFluid)) {
        tag = new NBTTagCompound
        fluidsTag.appendTag(tag)
      }
      //Else get existing NBT Tag from map
      else {
        val mapEntry = fluidsMap.get(stack.getFluid)
        tag = mapEntry.get._2
      }
      stack.writeToNBT(tag)
      fluidsMap.put(stack.getFluid, (stack, tag))
    }

    def getFluid(fluid: Fluid): Option[FluidStack] = {
      if (fluid == null)
        return None
      val value = fluidsMap.get(fluid)
      if (value.isEmpty)
        None
      else
        Some(value.get._1)
    }

    def removeFluid(fluid: Fluid): Boolean = {
      val value = fluidsMap.get(fluid)
      if (value.isEmpty)
        return false
      var tagIndex: Option[Int] = None
      for (i <- 0 until fluidsTag.tagCount if fluidsTag.getCompoundTagAt(i) == value.get._2)
        tagIndex = Some(i)
      if (tagIndex.isDefined)
        fluidsTag.removeTag(tagIndex.get)
      fluidsMap.remove(fluid)
      return true
    }

    def getSize = fluidsMap.size
  }
}
