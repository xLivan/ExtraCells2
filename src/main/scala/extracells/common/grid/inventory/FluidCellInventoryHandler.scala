package extracells.common.grid.inventory

import java.util.{ArrayList => JavaArrayList, List => JavaList}

import appeng.api.AEApi
import appeng.api.config.{AccessRestriction, Actionable}
import appeng.api.networking.security.BaseActionSource
import appeng.api.storage.data.{IAEFluidStack, IItemList}
import appeng.api.storage.{IMEInventoryHandler, ISaveProvider, StorageChannel}
import extracells.api.ECApi
import extracells.api.storage.filter.FilterType
import extracells.api.storage.{IFluidStorageCell, IHandlerFluidStorage}
import net.minecraft.inventory.IInventory
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
 * @param storageStack ItemStack of storage cell
 * @param saveProvider save provider
 */
//Todo: Add support for inverter cards, fuzzy cards make no sense here
class FluidCellInventoryHandler(storageStack: ItemStack, val saveProvider: ISaveProvider) extends IMEInventoryHandler[IAEFluidStack] with IHandlerFluidStorage{
  final private val tagVersionKey = "ec:tagVersion"
  final private val tagVersion = 1
  final private val tagStoredFluids = "ec:storedFluids"
  IInventory
  private val stackTag: NBTTagCompound = if (storageStack.hasTagCompound) storageStack.getTagCompound
    else {
      val tag: NBTTagCompound = new NBTTagCompound
      storageStack.setTagCompound(tag)
      tag
    }
  protected var storedFluids: FluidSet = _
  private var preformatList: JavaList[Fluid] = new JavaArrayList[Fluid]()
  var invertPreformat = false
  val bytesPerType: Int = storageStack.getItem.asInstanceOf[IFluidStorageCell].getBytesPerType(storageStack)
  val totalTypes: Int = storageStack.getItem.asInstanceOf[IFluidStorageCell].getMaxTypes(storageStack)
  val totalBytes: Int = storageStack.getItem.asInstanceOf[IFluidStorageCell].getMaxBytes(storageStack)

  loadNBTTag()

  def this(storageStack: ItemStack, saveProvider: ISaveProvider, preformat: JavaList[Fluid]) {
    this(storageStack, saveProvider)
    if (preformat != null)
      this.preformatList = preformat
  }


  override def injectItems(input: IAEFluidStack, mode: Actionable, source: BaseActionSource): IAEFluidStack = {
    lazy val storedFluid = this.storedFluids.getFluid(input.getFluid)
    var requiredBytes: Int = 0

    //Check if can inject
    if (input == null || !ECApi.instance.isFluidAllowed(FilterType.STORAGE, input.getFluid) ||
        !isAllowedByFormat(input.getFluid))
      return input
    if (storedFluid.isEmpty) {
      //Out of types, can't accept!
      if (this.storedFluids.getSize >= this.totalTypes)
        return input
      requiredBytes += bytesPerType
    }
    requiredBytes += input.getStackSize.toInt

    if (requiredBytes <= this.freeBytes) { //If required space fits in free space
      if (storedFluid.isEmpty) //If type is not already in storage
        if (mode == Actionable.MODULATE)
          this.storedFluids.updateFluid(input.getFluidStack)
      else { //If type is already in storage
        if (mode == Actionable.MODULATE) {
          val fluidStack = storedFluid.get
          fluidStack.amount += input.getStackSize.toInt
          this.storedFluids.updateFluid(fluidStack)
        }
      }
      null
    }
    else { //If it will not fit
      var amountInjected = 0
      if (storedFluid.isEmpty) { //If type is not already in storage
        amountInjected = this.freeBytes - this.bytesPerType
        if (mode == Actionable.MODULATE) {
          val fluidStack = new FluidStack(input.getFluid, amountInjected)
          this.storedFluids.updateFluid(fluidStack)
        }
      }
      else { //If type is already in storage
        amountInjected = this.freeBytes
        if (mode == Actionable.MODULATE) {
          val fluidStack = storedFluid.get
          fluidStack.amount += amountInjected
          this.storedFluids.updateFluid(fluidStack)
        }
      }
      input.setStackSize(input.getStackSize - amountInjected)
      input
    }
  }

  override def extractItems(request: IAEFluidStack, mode: Actionable, src: BaseActionSource): IAEFluidStack = {
    lazy val requestedFluid = this.storedFluids.getFluid(request.getFluid)
    lazy val extractedFluid = request.copy()

    if (request == null || requestedFluid.isEmpty)
      return null

    if (request.getStackSize < requestedFluid.get.amount) {
      val stack = requestedFluid.get
      if (mode == Actionable.MODULATE) {
        stack.amount -= request.getStackSize
        this.storedFluids.updateFluid(stack)
      }
      extractedFluid
    }
    else {
      val stack = requestedFluid.get
      extractedFluid.setStackSize(stack.amount)
      if (mode == Actionable.MODULATE) {
        this.storedFluids.removeFluid(stack.getFluid)
      }
      extractedFluid
    }
  }

  override def getAvailableItems(out: IItemList[IAEFluidStack]): IItemList[IAEFluidStack] = {
    for (stack: FluidStack <- this.storedFluids if stack != null)
      out.add(AEApi.instance.storage.createFluidStack(stack))
    out
  }

  /**
   * Checks if a fluid is allowed by performatting
   * @param input Input fluid
   * @return
   */
  def isAllowedByFormat(input: Fluid): Boolean = {
    val preformatCheck = if (this.invertPreformat) !this.preformatList.contains(input)
      else this.preformatList.contains(input)
    !this.isFormatted || preformatCheck
  }

  override def isPrioritized(input: IAEFluidStack): Boolean = input != null &&
    this.preformatList.contains(input.getFluid)
  override def isFormatted: Boolean = {
    if (this.preformatList.isEmpty)
      return false
    for (fluid: Fluid <- this.preformatList if fluid != null)
      return true
  }
  override def canAccept(input: IAEFluidStack): Boolean = {
    if (input == null)
      return false
    if (!ECApi.instance.isFluidAllowed(FilterType.STORAGE, input.getFluid))
      return false
    for (stack: FluidStack <- this.storedFluids)
      if (stack == null || stack.getFluid == input.getFluid)
        return this.isAllowedByFormat(input.getFluid)
    false
  }
  override def validForPass(i: Int): Boolean = true

  override def getSlot: Int = 0
  override def getPriority: Int = 0
  override def getAccess: AccessRestriction = AccessRestriction.READ_WRITE
  override def getChannel: StorageChannel = StorageChannel.FLUIDS

  /** Get free bytes in cell*/
  def freeBytes: Int = {
    var i: Int = 0
    for (stack: FluidStack <- this.storedFluids if stack != null)
      i += (stack.amount + this.bytesPerType)
    //In the event its custom NBT with more fluids then totalSize do not return negative.
    Math.max(0, this.totalBytes - i)
  }
  override def usedBytes(): Int = this.totalBytes - this.freeBytes
  override def usedTypes(): Int = this.storedFluids.getSize

  private def loadNBTTag(): Unit = {
    this.stackTag.getInteger(this.tagVersionKey) match {
      //Original EC2 format, maintained for backwards compatibility.
      case 0 => this.storedFluids = new FluidSet(new NBTTagList)
        loadLegacyTag()
      //Current format
      case 1 => this.storedFluids = new FluidSet(this.stackTag.getTagList(this.tagStoredFluids,Constants.NBT.TAG_COMPOUND))
    }
    this.stackTag.setTag(this.tagStoredFluids, this.storedFluids.fluidsTag)
  }

  private def loadLegacyTag(): Unit = {
    this.stackTag.setInteger(this.tagVersionKey, this.tagVersion)
    for (slot: Int <- 0 until this.totalTypes) {
      val tagKey = "Fluid#" + slot
      val fluidStack = FluidStack.loadFluidStackFromNBT(this.stackTag.getCompoundTag(tagKey))
      if (fluidStack != null)
        this.storedFluids.updateFluid(fluidStack)
      this.stackTag.removeTag(tagKey)
    }
  }

  /**
   * Basically a wrapper class that manages a NBTTagList
   * will update the NBT tag at the same time.
   *
   * If fluids changed from iterator,
   * call syncNBT to sync to NBT
   *
   * @param fluidsTag NBTTagList for the fluids
   */
  private class FluidSet(val fluidsTag: NBTTagList, val maxSize: Int = 63) extends mutable.Iterable[FluidStack] {
    private[this] val fluidsMap = new mutable.HashMap[Fluid, (FluidStack, NBTTagCompound)]()
    //63 Types max!
    for (i <- 0 until fluidsTag.tagCount()) {
      if (i <= maxSize) {
        val tag = fluidsTag.getCompoundTagAt(i)
        val fluidStack = FluidStack.loadFluidStackFromNBT(tag)
        if (fluidStack != null)
          fluidsMap.put(fluidStack.getFluid, (fluidStack, tag))
        else
          fluidsTag.removeTag(i)
      }
      else
        fluidsTag.removeTag(i)
    }

    def updateFluid(stack: FluidStack): Unit = {
      var tag: NBTTagCompound = null
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
      true
    }

    def getSize = fluidsMap.size

    def syncNBT(): Unit = for((stack, nbt) <- fluidsMap.values)
      stack.writeToNBT(nbt)

    override def iterator = fluidsMap.values.unzip._1.iterator
  }
}
