package extracells.common.container.implementations

import java.lang.Iterable

import appeng.api.AEApi
import appeng.api.config.Actionable
import appeng.api.networking.security.{PlayerSource, BaseActionSource}
import appeng.api.networking.storage.IBaseMonitor
import appeng.api.storage.{IMEMonitor, IMEMonitorHandlerReceiver}
import appeng.api.storage.data.{IAEFluidStack, IItemList}
import extracells.api.storage.{IWirelessFluidTermHandler, IPortableFluidStorageCell}
import extracells.common.container.ContainerECBase
import extracells.common.container.slot.{SlotPlayerInventory, SlotRespective}
import extracells.common.inventory.{TInventoryUpdateReceiver, ECInventoryBase}
import extracells.common.network.NetworkWrapper
import extracells.common.network.packet.PacketFluidStorage
import extracells.common.util.{FluidUtil, TFluidSelector}
import net.minecraft.entity.player.{InventoryPlayer, EntityPlayer}
import net.minecraft.inventory.SlotFurnace
import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.{FluidContainerRegistry, IFluidContainerItem, Fluid}
//TODO: Finish Implementing
class ContainerFluidStorage(val monitor: IMEMonitor[IAEFluidStack], val player: EntityPlayer) extends ContainerECBase
    with IMEMonitorHandlerReceiver[IAEFluidStack]
    with TInventoryUpdateReceiver
    with TFluidSelector {

  private val inventory: ECInventoryBase = new ECInventoryBase(
      "extracells.item.fluid.storage", 2, 64, this) {
    override def isItemValidForSlot(index: Int, stack: ItemStack): Boolean = FluidUtil.getFilledFluid(stack).isDefined
    override def isUseableByPlayer(player: EntityPlayer): Boolean = true
  }
  private var fluidStackList: IItemList[IAEFluidStack] = _
  private var isPortableTerminal = false
  private var wirelessTermHandler: IWirelessFluidTermHandler = _
  private var portableCell: IPortableFluidStorageCell = _
  private var selectedFluid: Fluid = _

  if (!this.player.worldObj.isRemote && this.monitor != null) {
    this.monitor.addListener(this, null)
    this.fluidStackList = this.monitor.getStorageList
  }
  else
    this.fluidStackList = AEApi.instance().storage().createFluidList()

  addSlotToContainer(new SlotRespective(this.inventory, 0, 8, 92))
  addSlotToContainer(new SlotFurnace(this.player, this.inventory, 1, 26, 92))
  bindPlayerInventory(this.player.inventory)

  def this(player: EntityPlayer) {
    this(null, player)
  }

  def this(monitor: IMEMonitor[IAEFluidStack], player: EntityPlayer, cell: IPortableFluidStorageCell) {
    this(monitor, player)
    this.isPortableTerminal = cell != null
    this.portableCell = cell
  }

  def this(monitor: IMEMonitor[IAEFluidStack], player: EntityPlayer, handler: IWirelessFluidTermHandler) {
    this(monitor, player)
    this.isPortableTerminal = handler != null
    this.wirelessTermHandler = handler
  }

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
        new PacketFluidStorage(this.player, this.monitor.getStorageList), this.player)
  }

  //TODO: Supply a proper IActionHost
  def doWork(): Unit = {
    val actionSource = new PlayerSource(this.player, null)
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
      val request = FluidUtil.createAEFluidStack(this.selectedFluid, amountToFill)
      val result = this.monitor.extractItems(request, Actionable.SIMULATE, actionSource)
      //Fill container.
      val fillResult = FluidUtil.fillFluidContainer(inStack, result.getFluidStack)
      //Commit to network.
      if (fillResult._1.eq(inStack) || !addToSlot(1, fillResult._1))
        return
      decrementSlot(0)
      this.monitor.extractItems(request, Actionable.MODULATE, actionSource)
      if (fillResult._2.isDefined)
        this.monitor.injectItems(FluidUtil.createAEFluidStack(fillResult._2.get), Actionable.MODULATE, actionSource)
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

  def hasWirelessTermHandler: Boolean = this.isPortableTerminal
  override def canInteractWith(player: EntityPlayer) : Boolean = {
    //Checks if the portable item still has enough power to stay active.
    if (hasWirelessTermHandler) {
      val stack = player.getCurrentEquippedItem
      if (portableCell != null) {
        if (stack.getItem.isInstanceOf[IPortableFluidStorageCell])
          return portableCell.hasPower(player, portableCell.getIdlePowerDrain(stack), stack)
        else
          return false
      }
      else if (wirelessTermHandler != null) {
        if (stack.getItem.isInstanceOf[IWirelessFluidTermHandler])
          return wirelessTermHandler.hasPower(player, wirelessTermHandler.getIdlePowerDrain(stack), stack)
        else
          return false
      }
    }
    true
  }
  override def isValid(verificationToken: scala.Any): Boolean = ???

  def receiveSelectedFluid(fluid: Fluid): Unit = ???
  def updateFluidList(fluidList: IItemList[IAEFluidStack]): Unit = ???
  override def setSelectedFluid(fluid: Fluid): Unit = ???

  override def onListUpdate(): Unit = ???
  override def onInventoryChanged(): Unit = ???
  override def postChange(monitor: IBaseMonitor[IAEFluidStack], change: Iterable[IAEFluidStack], actionSource: BaseActionSource): Unit = ???



}
