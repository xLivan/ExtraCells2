package extracells.client.gui.widget

import appeng.api.storage.data.IAEFluidStack
import extracells.common.util.TFluidSelector

trait TFluidSelectorGui extends TFluidWidgetGui{
  def getSelector: TFluidSelector
  def getCurrentFluid: IAEFluidStack
}
