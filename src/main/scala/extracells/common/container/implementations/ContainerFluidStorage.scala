package extracells.common.container.implementations

import appeng.api.storage.IMEMonitorHandlerReceiver
import appeng.api.storage.data.{IItemList, IAEFluidStack}
import extracells.client.gui.widget.TFluidSelectorContainer
import extracells.common.container.ContainerECBase
import extracells.common.util.{TFluidSelector, TInventoryUpdateReceiver}
import net.minecraft.inventory.Container
import net.minecraftforge.fluids.Fluid

class ContainerFluidStorage extends ContainerECBase
  with IMEMonitorHandlerReceiver[IAEFluidStack]
  with TFluidSelector
  with TInventoryUpdateReceiver {

  var hasWirelessTermHandler: Boolean = _

  def updateFluidList(fluidList: IItemList[IAEFluidStack]): Unit = ???
  def forceFluidUpdate(): Unit = ???
  def doWork(): Unit = ???
  def receiveSelectedFluid(fluid: Fluid): Unit = ???

}
