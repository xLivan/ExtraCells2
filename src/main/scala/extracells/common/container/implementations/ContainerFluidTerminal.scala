package extracells.common.container.implementations

import appeng.api.networking.storage.IStorageGrid
import appeng.api.storage.IMEMonitor
import appeng.api.storage.data.IAEFluidStack
import extracells.common.inventory.ECInventoryBase
import extracells.common.part.PartFluidTerminal
import extracells.common.util.FluidUtil
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack

class ContainerFluidTerminal(val term: PartFluidTerminal,
                             player: EntityPlayer,
                             monitor: IMEMonitor[IAEFluidStack])
    extends ContainerFluidStorage(monitor ,player, term) {

  override val inventory = new ECInventoryBase(
    "extracells.part.fluid.terminal", 2, 64, this) {
    override def isItemValidForSlot(index: Int, stack: ItemStack): Boolean = {
      FluidUtil.isFluidContainer(stack)
    }

    override def isUseableByPlayer(player: EntityPlayer): Boolean = term.isValid
  }

  term.containers.append(this)

  def this(term: PartFluidTerminal, player: EntityPlayer) {
    this(term, player, term.getProxy.flatMap(p => Option(p.getGridNode))
      .map(_.getGrid)
      .map(_.getCache[IStorageGrid](classOf[IStorageGrid]))
      .map(_.getFluidInventory)
      .orNull)
  }

  override def canInteractWith(player: EntityPlayer): Boolean = term.isValid

  override def isValid(verificationToken: scala.Any): Boolean = term.isValid && isActive

  override def onContainerClosed(player: EntityPlayer): Unit = {
    super.onContainerClosed(player)
    term.containers.remove(term.containers.indexOf(this))
    if (player.worldObj.isRemote)
      return
    for (i <- 0 until inventory.getSizeInventory)
      Option(inventory.getStackInSlotOnClosing(i))
        .foreach(s => player.dropPlayerItemWithRandomChoice(s, true))
  }

  override def onInventoryChanged(): Unit = {
    term.alertPart()
  }
}
