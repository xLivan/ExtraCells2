package extracells.common.container.implementations

import java.lang.Iterable

import appeng.api.config.Actionable
import appeng.api.networking.security.{BaseActionSource, IActionHost, PlayerSource}
import appeng.api.networking.storage.IBaseMonitor
import appeng.api.storage.data.IAEFluidStack
import appeng.api.storage.{IMEMonitor, IMEMonitorHandlerReceiver}
import extracells.client.gui.GuiFluidStorage
import extracells.common.container.ContainerECBase
import extracells.common.container.slot.{SlotPlayerInventory, SlotRespective}
import extracells.common.inventory.{ECInventoryBase, TInventoryUpdateReceiver}
import extracells.common.network.NetworkWrapper
import extracells.common.network.packet.PacketFluidStorage
import extracells.common.util.{ConversionUtil, FluidUtil, TFluidSelector}
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.inventory.{Slot, SlotFurnace}
import net.minecraft.item.ItemStack
import net.minecraftforge.fluids._

import scala.collection.immutable

abstract class ContainerFluidStorage(mon: IMEMonitor[IAEFluidStack],
                            val player: EntityPlayer,
                            val actionHost: IActionHost) extends ContainerECBase[GuiFluidStorage]
    with IMEMonitorHandlerReceiver[IAEFluidStack]
    with TInventoryUpdateReceiver
    with TFluidSelector {

  val monitor = Option(mon)
  val inventory: ECInventoryBase
  var isActive = true
  private var fluidStackList: immutable.List[IAEFluidStack] = _
  private var selectedFluid: Option[Fluid] = None
  private var selectedFluidStack: Option[IAEFluidStack] = None

  if (!this.player.worldObj.isRemote) {
    this.monitor.foreach(_.addListener(this, null))
    fluidStackList = this.getStorageList
  }

  addInvSlots()
  bindPlayerInventory(this.player.inventory)

  protected def addInvSlots(): Unit = {
    addSlotToContainer(new SlotRespective(this.inventory, 0, 8, 92))
    addSlotToContainer(new SlotFurnace(this.player, this.inventory, 1, 26, 92))
  }

  /** Bind the player inventory to container slots */
  protected def bindPlayerInventory(inv: InventoryPlayer): Unit = {
    for (i <- 0 until 3; j <- 0 until 9)
      addSlotToContainer(new SlotPlayerInventory(inv, this,
        j + i * 9 + 9, 8 + j *18, i * 18 + 122))

    for (i <- 0 until 9)
      addSlotToContainer(new SlotPlayerInventory(inv, this,
        i, 8 + i * 18, 180))
  }

  /**
   * Send update packets
   */
  def forceFluidUpdate(): Unit = {
    if (this.monitor != null)
      NetworkWrapper.sendToPlayer(
        new PacketFluidStorage(this.getStorageList), this.player)
  }

  private def getStorageList = this.monitor.map( m =>
    ConversionUtil.AEListToScalaList(m.getStorageList))
    .getOrElse(immutable.List[IAEFluidStack]())

  def hasWork: Boolean = {
    val inStack = this.inventory.getStackInSlot(0).copy()
    val outStack = this.inventory.getStackInSlot(1).copy()
    if (this.monitor.isEmpty || this.selectedFluid == null)
      return false
    if (!FluidUtil.isFluidContainer(inStack))
      return false
    if (outStack != null && (!outStack.isStackable || outStack.stackSize >= outStack.getMaxStackSize))
      return false
    true
  }

  /**
   *  Fill and drain containers.
   *  @return If it managed to do any work.
   */
  def doWork(): Boolean = {
    val actionSource = new PlayerSource(this.player, actionHost)
    val inStack = this.inventory.getStackInSlot(0).copy()
    if (!hasWork)
      return false
    val mon = this.monitor.get
    inStack.stackSize = 1
    if (!FluidUtil.isFullFluidContainer(inStack)) {
      val amountToFill = if (FluidUtil.isEmptyFluidContainer(inStack)) FluidUtil.getContainerCapacity(inStack)
        else FluidUtil.getContainerCapacity(inStack) - FluidUtil.getFilledFluid(inStack).get.amount
      val request = FluidUtil.createAEFluidStack(this.selectedFluid.orNull, amountToFill)
      val result = mon.extractItems(request, Actionable.SIMULATE, actionSource)
      //Fill container.
      val fillResult = FluidUtil.fillFluidContainer(inStack, result.getFluidStack)
      //Commit to network.
      if (fillResult._1.exists(_ eq inStack) || fillResult._1.exists(s => !addToSlot(1, s)))
        return false
      decrementSlot(0)
      mon.extractItems(request, Actionable.MODULATE, actionSource)
      fillResult._2.foreach( stack => mon.injectItems(
        FluidUtil.createAEFluidStack(stack), Actionable.MODULATE, actionSource))
      true
    }
    // Full fluid containers, drain to network.
    else {
      inStack.getItem match {
        case item: IFluidContainerItem => val drained = item.drain(inStack, item.getCapacity(inStack), false)
          val overspill = mon.injectItems(FluidUtil.createAEFluidStack(drained),
            Actionable.SIMULATE, actionSource)
          val drainAmount = if (overspill == null) 0 else drained.amount - overspill.getStackSize.toInt
          if (drainAmount == 0)
            return false
          val removedFluid = FluidUtil.unifyStack(item.drain(inStack, drainAmount, true))
          if (!addToSlot(1, inStack))
            return false
          decrementSlot(0)
          mon.injectItems(FluidUtil.createAEFluidStack(removedFluid),
            Actionable.MODULATE, actionSource)
          true

        case _ => if (!FluidContainerRegistry.isFilledContainer(inStack)) return false
          val fluidContents = FluidUtil.unifyStack(FluidUtil.getFilledFluid(inStack))
          val overspill = mon.injectItems(FluidUtil.createAEFluidStack(fluidContents.get),
            Actionable.SIMULATE, actionSource)
          if (overspill != null && overspill.getStackSize != 0)
            return false
          val empty = FluidContainerRegistry.drainFluidContainer(inStack)
          if (empty == null || !addToSlot(1, empty))
            return false
          decrementSlot(0)
          mon.injectItems(FluidUtil.createAEFluidStack(fluidContents.get),
            Actionable.MODULATE, actionSource)
          true
      }
    }
  }

  override def transferStackInSlot(player: EntityPlayer, slotNum: Int): ItemStack = {
    var remainder: ItemStack = null
    val slot: Slot = this.getSlot(slotNum)
    if (slot != null && slot.getHasStack) {
      val slotStack = slot.getStack
      remainder = slotStack.copy()
      if (this.inventory.isItemValidForSlot(0, remainder)) {
        if (List(0,1).contains(slotNum))
          if (!mergeItemStack(remainder, 2, 36, false))
            return null
        else if (!mergeItemStack(remainder, 0, 0, false))
            return null

        if (remainder.stackSize == 0)
          slot.putStack(null)
        else
          slot.onSlotChanged()
      }
      else
        return null
    }
    remainder
  }

  /**
   * Add itemStack to slot
   * @param index slot to add to
   * @param stack stack to add
   * @return if successfully added to slot
   */
  def addToSlot(index: Int, stack: ItemStack): Boolean = {
    if (!(index < this.inventory.slots.length))
      return false
    if (this.inventory.getStackInSlot(index) == null) {
      this.inventory.setInventorySlotContents(index, stack)
      true
    }
    else {
      val slotStack = this.inventory.getStackInSlot(index)
      if (!slotStack.isItemEqual(stack) || !ItemStack.areItemStackTagsEqual(slotStack, stack))
        return false
      if (!slotStack.isStackable || (slotStack.stackSize + stack.stackSize) > slotStack.getMaxStackSize)
        return false
      slotStack.stackSize += 1
      this.inventory.setInventorySlotContents(index, slotStack)
      true
    }
  }

  /**
   * Decrement the itemstack in a slot by 1
   * @param index slot to decrement
   */
  def decrementSlot(index: Int): Unit = {
    if (!(index < this.inventory.slots.length))
      return
    val stack = this.inventory.getStackInSlot(index)
    if (stack == null)
      return
    stack.stackSize -= 1
    if (stack.stackSize <= 0)
      this.inventory.setInventorySlotContents(index, null)
    this.inventory.setInventorySlotContents(index, stack)
  }

  /** Update the selected fluid */
  def receiveSelectedFluid(fluid: Fluid): Unit = {
    this.selectedFluid = Option(fluid)
    this.selectedFluidStack = None
    this.selectedFluid.foreach( fluid => this.fluidStackList
      .withFilter(_.ne(null))
      .withFilter(_.getFluid.eq(fluid)) //Should only match one entry.
      .foreach(stack => this.selectedFluidStack = Option(stack))
    )
    this.gui.foreach(_.updateFluids())
  }

  /** Update the fluid list */
  def updateFluidList(fluidList: immutable.List[IAEFluidStack]): Unit = {
    this.fluidStackList = fluidList
    this.gui.foreach(_.updateFluids())
  }

  override def setSelectedFluid(fluid: Fluid): Unit = {
    NetworkWrapper.sendToServer(new PacketFluidStorage(fluid))
    receiveSelectedFluid(fluid)
  }

  override def onListUpdate(): Unit = {}
  override def onInventoryChanged(): Unit = {}
  override def onContainerClosed(player: EntityPlayer): Unit = {
    this.isActive = false
  }

  override def postChange(monitor: IBaseMonitor[IAEFluidStack],
                          change: Iterable[IAEFluidStack],
                          actionSource: BaseActionSource): Unit = {
    this.fluidStackList = ConversionUtil.AEListToScalaList(monitor
      .asInstanceOf[IMEMonitor[IAEFluidStack]].getStorageList)
    NetworkWrapper.sendToPlayer(new PacketFluidStorage(this.fluidStackList), this.player)
  }


}
