package extracells.common.util

import appeng.api.AEApi
import appeng.api.storage.data.IAEFluidStack
import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.{IFluidContainerItem, FluidContainerRegistry, Fluid, FluidStack}

object FluidUtil {

  /**
   * Creates a IAEFluidStack from a Fluid and Amount
   * @param fluid Fluid Definition
   * @param amount Amount of Fluid (Defaults to BUCKET_VOLUME)
   * @return
   */
  def createAEFluidStack(fluid: Fluid, amount: Int = FluidContainerRegistry.BUCKET_VOLUME): IAEFluidStack =
    createAEFluidStack(new FluidStack(fluid, amount))

  /**
   * Creates a IAEFluidStack from a FluidStack
   * @param stack Fluid stack to use.
   * @return
   */
  def createAEFluidStack(stack: FluidStack): IAEFluidStack =
    AEApi.instance.storage.createFluidStack(stack)

  /**
   * Gets the currently filled fluid for a container
   * @param stack itemstack of container.
   * @return
   */
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

  /**
   * Checks if a given itemstack is a container.
   * @param stack itemstack to check.
   * @return
   */
  def isFluidContainer(stack: ItemStack): Boolean = {
    if (stack == null)
      return false
    stack.getItem match {
      case item: IFluidContainerItem => true
      case _ => FluidContainerRegistry.isContainer(stack)
    }
  }

  /**
   * Checks if a given fluid container is filled to capacity.
   * @param stack container itemstack to check.
   * @return
   */
  def isFullFluidContainer(stack: ItemStack): Boolean = stack.getItem match {
    case item: IFluidContainerItem => item.getFluid(stack).amount == item.getCapacity(stack)
    case _ => if (FluidContainerRegistry.isFilledContainer(stack)) {
      FluidContainerRegistry.getFluidForFilledItem(stack).amount == FluidContainerRegistry.getContainerCapacity(stack)}
      else false
  }

  /**
   * Attempts to fill a container with a fluid.
   * @param stack ItemStack of container.
   * @param fluid FluidStack to fill into container.
   * @return Filled container and Remaining fluid not filled. None if all has been filled into container.
   */
  def fillFluidContainer(stack: ItemStack, fluid: FluidStack): (ItemStack, Option[FluidStack]) = {
    var changedStack: ItemStack = null
    stack.getItem match {
      case item: IFluidContainerItem => val filled = item.fill(changedStack, fluid, true)
        changedStack = stack.copy()
        if (filled < fluid.amount) {
          val remainder = fluid.copy()
          remainder.amount -= filled
          (changedStack, Option(remainder))
        }
        else {
          (changedStack, None)
        }
        //If fluid to fill is less then container capacity or container is already filled.
      case _ => if (FluidContainerRegistry.isEmptyContainer(stack) &&
        fluid.amount >= FluidContainerRegistry.getContainerCapacity(fluid, stack)) {

          changedStack = FluidContainerRegistry.fillFluidContainer(fluid, stack)
          val filledFluid = FluidContainerRegistry.getFluidForFilledItem(changedStack)

          if (fluid.amount > filledFluid.amount) {
            val remainder = fluid.copy()
            remainder.amount = fluid.amount - filledFluid.amount
            (changedStack, Option(remainder))
          }
          else (changedStack, None)
      }
      else (stack, Option(fluid))
    }
  }
}
