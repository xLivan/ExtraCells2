package extracells.common.part

import appeng.api.config.Actionable
import appeng.api.networking.security.MachineSource
import appeng.api.storage.IMEMonitor
import appeng.api.storage.data.IAEFluidStack
import net.minecraftforge.fluids.IFluidHandler

abstract class PartECFluidBase extends PartECBase {
  protected var facingTank : IFluidHandler = _

  protected final def injectFluid(toInject: IAEFluidStack, action: Actionable) : IAEFluidStack = {
    if (this.proxy == null || this.facingTank == null)
      return toInject
    val fluidStorage: IMEMonitor[IAEFluidStack] = this.proxy.getStorage.getFluidInventory
    if (fluidStorage == null)
      return toInject
    return fluidStorage.injectItems(toInject, action, new MachineSource(this))
  }

  protected final def extractFluid(toExtract: IAEFluidStack, action: Actionable): IAEFluidStack = {
    if (this.proxy ==  null || this.facingTank == null)
      return null
    val fluidStorage: IMEMonitor[IAEFluidStack] = this.proxy.getStorage.getFluidInventory
    if (fluidStorage == null)
      return null
    return fluidStorage.extractItems(toExtract, action, new MachineSource(this))
  }

  protected final def getFacingTank : IFluidHandler = this.facingTank
}
