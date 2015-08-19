package extracells.common.container.implementations

import appeng.api.storage.IMEMonitorHandlerReceiver
import appeng.api.storage.data.IAEFluidStack
import extracells.common.container.TFluidSelectorContainer
import extracells.common.util.TInventoryUpdateReceiver
import net.minecraft.inventory.Container

class ContainerFluidStorage extends Container
    with IMEMonitorHandlerReceiver[IAEFluidStack]
    with TFluidSelectorContainer
    with TInventoryUpdateReceiver {


}
