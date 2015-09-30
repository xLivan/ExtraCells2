package extracells.core.common.part

import appeng.api.config.Actionable
import appeng.api.networking.security
import appeng.api.networking.security.{BaseActionSource, MachineSource}
import appeng.api.storage.IMEMonitor
import appeng.api.storage.data.IAEFluidStack
import net.minecraftforge.fluids.IFluidHandler

abstract class PartECFluidBase extends PartECBase {
  protected final def injectFluid(toInject: IAEFluidStack,
                                  action: Actionable,
                                  source: BaseActionSource =
                                    new security.MachineSource(this)): Option[IAEFluidStack] = {
    for {
      i <- Option(toInject)
      m <- this.getProxy.flatMap(_.getStorage).flatMap(sg => Option(sg.getFluidInventory))
    } yield m.injectItems(i, action, source)
  }

  protected final def extractFluid(toExtract: IAEFluidStack,
                                   action: Actionable,
                                   source: BaseActionSource =
                                    new security.MachineSource(this)): Option[IAEFluidStack]= {
    for {
      e <- Option(toExtract)
      m <- this.getProxy.flatMap(_.getStorage).flatMap(sg => Option(sg.getFluidInventory))
    } yield m.extractItems(e, action, source)
  }
}
