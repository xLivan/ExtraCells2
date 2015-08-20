package extracells.common.container.implementations

import java.lang.Iterable

import appeng.api.networking.security.BaseActionSource
import appeng.api.networking.storage.IBaseMonitor
import appeng.api.storage.IMEMonitorHandlerReceiver
import appeng.api.storage.data.{IAEFluidStack, IItemList}
import extracells.common.container.ContainerECBase
import extracells.common.util.{TFluidSelector, TInventoryUpdateReceiver}
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fluids.Fluid

class ContainerFluidStorage extends ContainerECBase
  with IMEMonitorHandlerReceiver[IAEFluidStack]
  with TFluidSelector
  with TInventoryUpdateReceiver {

  var hasWirelessTermHandler: Boolean = _

  def forceFluidUpdate(): Unit = ???
  def doWork(): Unit = ???

  override def canInteractWith(player: EntityPlayer) : Boolean = ???
  override def isValid(verificationToken: scala.Any): Boolean = ???

  def receiveSelectedFluid(fluid: Fluid): Unit = ???
  override def setSelectedFluid(fluid: Fluid): Unit = ???
  def updateFluidList(fluidList: IItemList[IAEFluidStack]): Unit = ???

  override def onListUpdate(): Unit = ???
  override def onInventoryChanged(): Unit = ???
  override def postChange(monitor: IBaseMonitor[IAEFluidStack], change: Iterable[IAEFluidStack], actionSource: BaseActionSource): Unit = ???



}
