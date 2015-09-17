package extracells.common.container.implementations

import java.lang.Iterable

import appeng.api.config.Actionable
import appeng.api.networking.security.{BaseActionSource, IActionHost, PlayerSource}
import appeng.api.networking.storage.IBaseMonitor
import appeng.api.storage.data.{IAEFluidStack, IItemList}
import appeng.api.storage.{IMEMonitor, IMEMonitorHandlerReceiver}
import extracells.api.storage.{IPortableFluidStorageCell, IPortablePoweredDevice, IWirelessFluidTermHandler}
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
import net.minecraftforge.fluids.{Fluid, FluidContainerRegistry, IFluidContainerItem}

import scala.collection.immutable

class ContainerFluidStorage(val monitor: IMEMonitor[IAEFluidStack],
                            val player: EntityPlayer,
                            val actionHost: IActionHost = null) extends ContainerECBase[GuiFluidStorage]
    with IMEMonitorHandlerReceiver[IAEFluidStack]
    with TInventoryUpdateReceiver
    with TFluidSelector {

  private val inventory: ECInventoryBase = new ECInventoryBase(
      "extracells.item.fluid.storage", 2, 64, this) {
    override def isItemValidForSlot(index: Int, stack: ItemStack): Boolean = FluidUtil.isFluidContainer(stack)
    override def isUseableByPlayer(player: EntityPlayer): Boolean = true
  }
  private var fluidStackList: immutable.List[IAEFluidStack] = _
  private var isPortableTerminal = false
  private var wirelessTermHandler: Option[IWirelessFluidTermHandler] = None
  private var portableCell: Option[IPortableFluidStorageCell] = None
  private var selectedFluid: Option[Fluid] = None
  private var selectedFluidStack: Option[IAEFluidStack] = None

  if (!this.player.worldObj.isRemote && this.monitor != null) {
    this.monitor.addListener(this, null)
    fluidStackList = ConversionUtil.AEListToScalaList(this.monitor.getStorageList)
  }

  addSlotToContainer(new SlotRespective(this.inventory, 0, 8, 92))
  addSlotToContainer(new SlotFurnace(this.player, this.inventory, 1, 26, 92))
  bindPlayerInventory(this.player.inventory)

  /**
   * Constructor
   * @param monitor AE Monitor for Storage Access
   * @param player Player accessing
   */
  def this(monitor: IMEMonitor[IAEFluidStack], player: EntityPlayer) {
    this(monitor, player)
  }

  /**
   * Constructor
   * @param player Player Accessing
   */
  def this(player: EntityPlayer) {
    this(null, player)
  }

  /**
   * Constructor for use in portable cell
   * @param monitor AE Monitor for Storage Access
   * @param player Player accessing
   * @param cell Portable Fluid Cell instance
   */
  def this(monitor: IMEMonitor[IAEFluidStack], player: EntityPlayer, cell: IPortableFluidStorageCell) {
    this(monitor, player)
    this.isPortableTerminal = cell != null
    this.portableCell = Option(cell)
  }

  /**
   * Constructor for use in wireless terminal
   * @param monitor AE Monitor for Storage Access
   * @param player Player accessing
   * @param handler Wireless Terminal handler
   */
  def this(monitor: IMEMonitor[IAEFluidStack], player: EntityPlayer, handler: IWirelessFluidTermHandler) {
    this(monitor, player)
    this.isPortableTerminal = handler != null
    this.wirelessTermHandler = Option(handler)
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
        new PacketFluidStorage(ConversionUtil.AEListToScalaList(
          this.monitor.getStorageList)), this.player)
  }

  /** Energy ticking */
  def energyTick(): Unit = {
    var device: Option[IPortablePoweredDevice] = None
    var drainAmount: Double = 0
    if (this.wirelessTermHandler.isDefined) {
      device = this.wirelessTermHandler
      drainAmount = 1.0D
    }
    else if (this.portableCell.isDefined) {
      device = this.portableCell
      drainAmount = 0.5D
    }

    device.filter(_.hasPower(this.player, drainAmount,
        this.player.getCurrentEquippedItem))
      .foreach(_.usePower(this.player, drainAmount,
        this.player.getCurrentEquippedItem))
  }

  /** Fill and drain containers. */
  def doWork(): Unit = {
    val actionSource = new PlayerSource(this.player, actionHost)
    val inStack = this.inventory.getStackInSlot(0).copy()
    val outStack = this.inventory.getStackInSlot(1).copy()
    if (this.monitor == null || this.selectedFluid == null)
      return
    if (!FluidUtil.isFluidContainer(inStack))
      return
    if (outStack != null && (!outStack.isStackable || outStack.stackSize >= outStack.getMaxStackSize))
      return

    inStack.stackSize = 1
    if (!FluidUtil.isFullFluidContainer(inStack)) {
      val amountToFill = if (FluidUtil.isEmptyFluidContainer(inStack)) FluidUtil.getContainerCapacity(inStack)
        else FluidUtil.getContainerCapacity(inStack) - FluidUtil.getFilledFluid(inStack).get.amount
      val request = FluidUtil.createAEFluidStack(this.selectedFluid.orNull, amountToFill)
      val result = this.monitor.extractItems(request, Actionable.SIMULATE, actionSource)
      //Fill container.
      val fillResult = FluidUtil.fillFluidContainer(inStack, result.getFluidStack)
      //Commit to network.
      if (fillResult._1.eq(inStack) || !addToSlot(1, fillResult._1))
        return
      decrementSlot(0)
      this.monitor.extractItems(request, Actionable.MODULATE, actionSource)
      fillResult._2.foreach( stack => this.monitor.injectItems(
        FluidUtil.createAEFluidStack(stack), Actionable.MODULATE, actionSource))
    }
    // Full fluid containers, drain to network.
    else {
      inStack.getItem match {
        case item: IFluidContainerItem => val drained = item.drain(inStack, item.getCapacity(inStack), false)
          val overspill = this.monitor.injectItems(FluidUtil.createAEFluidStack(drained),
            Actionable.SIMULATE, actionSource)
          val drainAmount = if (overspill == null) 0 else drained.amount - overspill.getStackSize.toInt
          if (drainAmount == 0)
            return
          val removedFluid = item.drain(inStack, drainAmount, true)
          if (!addToSlot(1, inStack))
            return
          decrementSlot(0)
          this.monitor.injectItems(FluidUtil.createAEFluidStack(removedFluid), Actionable.MODULATE, actionSource)

        case _ => if (!FluidContainerRegistry.isFilledContainer(inStack)) return
          val fluidContents = FluidUtil.getFilledFluid(inStack)
          val overspill = this.monitor.injectItems(FluidUtil.createAEFluidStack(fluidContents.get),
            Actionable.SIMULATE, actionSource)
          if (overspill != null && overspill.getStackSize != 0)
            return
          val empty = FluidContainerRegistry.drainFluidContainer(inStack)
          if (empty == null || !addToSlot(1, empty))
            return
          decrementSlot(0)
          this.monitor.injectItems(FluidUtil.createAEFluidStack(fluidContents.get),
            Actionable.MODULATE, actionSource)
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
   * @return
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

  override def isValid(verificationToken: scala.Any): Boolean = true
  def hasWirelessTermHandler: Boolean = this.isPortableTerminal
  override def canInteractWith(player: EntityPlayer) : Boolean = {
    //Checks if the portable item still has enough power to stay active.
    if (hasWirelessTermHandler) {
      val stack = player.getCurrentEquippedItem
      return portableCell.exists(cell => cell.hasPower(player, cell.getIdlePowerDrain(stack), stack)) ||
        wirelessTermHandler.exists(term => term.hasPower(player, term.getIdlePowerDrain(stack), stack))
    }
    true
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
  def updateFluidList(fluidList: IItemList[IAEFluidStack]): Unit = {
    this.fluidStackList = ConversionUtil.AEListToScalaList[IAEFluidStack](fluidList)
    this.gui.foreach(_.updateFluids())
  }

  override def setSelectedFluid(fluid: Fluid): Unit = {
    NetworkWrapper.sendToServer(new PacketFluidStorage(fluid))
    receiveSelectedFluid(fluid)
  }

  override def onListUpdate(): Unit = {}
  override def onInventoryChanged(): Unit = {}

  override def postChange(monitor: IBaseMonitor[IAEFluidStack],
                          change: Iterable[IAEFluidStack],
                          actionSource: BaseActionSource): Unit = {
    this.fluidStackList = ConversionUtil.AEListToScalaList(monitor
      .asInstanceOf[IMEMonitor[IAEFluidStack]].getStorageList)
    NetworkWrapper.sendToPlayer(new PacketFluidStorage(this.fluidStackList), this.player)
  }


}
