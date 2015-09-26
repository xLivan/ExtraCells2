package extracells.common.util

import appeng.api.AEApi
import appeng.api.storage.data.IAEFluidStack
import net.minecraft.item.ItemStack
import net.minecraftforge.fluids._

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
   * Unifies fluid stacks to the default [[Fluid]] registered to the name in the [[FluidRegistry]]
   * @param stack [[Option]] containing the FluidStack to unify.
   * @return Option containing the unified FluidStack
   */
  def unifyStack(stack: Option[FluidStack]): Option[FluidStack] = stack.map( s =>
    if (FluidRegistry.isFluidDefault(s.getFluid))
      s
    else
      new FluidStack(getDefaultFluid(s.getFluid), s.amount, s.tag)
  )

  /**
   * Unifies fluid stacks to the default [[Fluid]] registered to the name in the [[FluidRegistry]]
   * @param stack FluidStack to unify.
   * @return Unified FluidStack
   */
  def unifyStack(stack: FluidStack): FluidStack = unifyStack(Option(stack)).orNull

  /**
   * Gets the default Fluid for the given fluid name.
   */
  def getDefaultFluid(fluid: Fluid): Fluid = if (FluidRegistry.isFluidDefault(fluid)) fluid
    else FluidRegistry.getFluid(fluid.getName)

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
   * Checks if a given itemstack is a empty fluid container.
   * @param stack [[ItemStack]] to check.
   * @return
   */
  def isEmptyFluidContainer(stack: ItemStack): Boolean = {
    if (stack == null)
      return false
    stack.getItem match {
      case item: IFluidContainerItem => val fluid = item.getFluid(stack)
        fluid == null || fluid.amount <= 0
      case _ => FluidContainerRegistry.isEmptyContainer(stack)
    }
  }

  /**
   * Checks if a given itemstack is a full fluid container.
   * @param stack container itemstack to check.
   * @return
   */
  def isFullFluidContainer(stack: ItemStack): Boolean = {
    if (stack == null)
      return false
    stack.getItem match {
      case item: IFluidContainerItem => item.getFluid(stack).amount == item.getCapacity(stack)
      case _ => FluidContainerRegistry.isFilledContainer(stack)
    }
  }

  /**
   * Gets the fluid capacity of a container ItemStack
   * @param stack Stack to check.
   * @return Capacity of the container.
   */
  def getContainerCapacity(stack: ItemStack): Int = {
    if (stack == null)
      return 0
    stack.getItem match {
      case item: IFluidContainerItem => item.getCapacity(stack)
      case _ => FluidContainerRegistry.getContainerCapacity(stack)
    }
  }

  /**
   * Attempts to fill a container with a fluid.
   * @param stack ItemStack of container.
   * @param fluid FluidStack to fill into container.
   * @return Filled container and Remaining fluid not filled. None if all has been filled into container.
   */
  def fillFluidContainer(stack: ItemStack, fluid: FluidStack): (ItemStack, Option[FluidStack]) = {
    if (stack == null || fluid == null)
      return (stack, Option(fluid))
    var changedStack: ItemStack = null
    stack.getItem match {
      //Handle IFluidContainerItems
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
      //Handle every other type of itemstack.
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

  /**
   * Drains the contents of a fluid container.
   * @param stack ItemStack representing a container/
   * @param maxDrain Amount of fluid to drain
   * @return The drained container and the fluid actually drained. None if none is drained.
   */
  def drainFluidContainer(stack: ItemStack, maxDrain: Int): (ItemStack, Option[FluidStack]) = {
    if (stack == null || maxDrain < 0)
      return (stack, None)
    var changedStack: ItemStack = null
    stack.getItem match {
      //Handle IFluidContainerItem
      case null => (stack, None)
      case item: IFluidContainerItem => changedStack = stack.copy()
        val drained = item.drain(changedStack, maxDrain, true)
        if (drained == null || drained.amount == 0)
          (stack, None)
        else
          (changedStack, Option(drained))
      //Handle basic containers.
      case _ => if (FluidContainerRegistry.isFilledContainer(stack)) {
        val drained = FluidContainerRegistry.getFluidForFilledItem(stack)
        changedStack = FluidContainerRegistry.drainFluidContainer(stack)
        //Failed to drain or container capacity is more then maxDrain parameter.
        if (changedStack == null || maxDrain < drained.amount)
          (stack, None)
        else
          (changedStack, Option(drained))
      }
      else (stack, None) //Not a filled container.
    }
  }
}
