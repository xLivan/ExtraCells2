package extracells.common.util

import appeng.api.AEApi
import appeng.api.storage.data.IAEFluidStack
import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.{IFluidContainerItem, FluidContainerRegistry, Fluid, FluidStack}

object FluidUtil {

  def createAEFluidStack(fluid: Fluid, amount: Int = FluidContainerRegistry.BUCKET_VOLUME): IAEFluidStack =
    createAEFluidStack(new FluidStack(fluid, amount))

  def createAEFluidStack(stack: FluidStack): IAEFluidStack =
    AEApi.instance.storage.createFluidStack(stack)

  def getFilledFluid(stack: ItemStack): Option[FluidStack] = {
    if (stack == null)
      return None
    stack.getItem match {
      case item: IFluidContainerItem => val content = item.getFluid(stack)
        if (content != null || content.amount > 0)
          return Option(content)
        None
      case _ => Option(FluidContainerRegistry.getFluidForFilledItem(stack))
   }
  }
}
