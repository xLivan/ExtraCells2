package extracells.core.common.util

import appeng.api.AEApi
import appeng.api.storage.data.IAEFluidStack
import extracells.core.common.registries.ItemEnum
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
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
   * Gets the default Fluid for the given fluid.
   */
  def getDefaultFluid(fluid: Fluid): Fluid = if (FluidRegistry.isFluidDefault(fluid)) fluid
    else FluidRegistry.getFluid(fluid.getName)

  /**
   * Gets the currently filled fluid for a container
   * @param stack itemstack of container.
   * @return
   */
  def getFilledFluid(stack: ItemStack): Option[FluidStack] = {
    Option(stack).flatMap(s => Option(s.getItem)).flatMap[FluidStack] {
      case item: IFluidContainerItem => Option(item.getFluid(stack))
      case _ => Option(FluidContainerRegistry.getFluidForFilledItem(stack))
   }.filter(_.amount > 0)
  }

  /**
   * Checks if a given itemstack is a container.
   * @param stack itemstack to check.
   * @return
   */
  def isFluidContainer(stack: ItemStack): Boolean = {
    Option(stack).flatMap(s => Option(s.getItem)).exists {
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
    Option(stack).flatMap(s => Option(s.getItem)).exists {
      case item: IFluidContainerItem => Option(item.getFluid(stack))
        .fold(true)(_.amount <= 0)
      case _ => FluidContainerRegistry.isEmptyContainer(stack)
    }
  }

  /**
   * Checks if a given itemstack is a full fluid container.
   * @param stack container itemstack to check.
   * @return
   */
  def isFullFluidContainer(stack: ItemStack): Boolean = {
    Option(stack).flatMap(s => Option(s.getItem)).exists {
      case item: IFluidContainerItem => item.getFluid(stack).amount == item.getCapacity(stack)
      case _ => FluidContainerRegistry.isFilledContainer(stack)
    }
  }

  /**
   * Checks if a given container contains any fluid, but is not full.
   */
  def isPartiallyFilledContainer(stack: ItemStack): Boolean = {
    Option(stack).flatMap(s => Option(s.getItem)).exists {
      case item: IFluidContainerItem => getFilledFluid(stack).exists(_.amount < item.getCapacity(stack))
      case _ => false
    }
  }

  def canFillContainer(stack: ItemStack): Boolean = {
    Option(stack).flatMap(s => Option(s.getItem)).exists {
      case item: IFluidContainerItem => isEmptyFluidContainer(stack) || isPartiallyFilledContainer(stack)
      case _ if FluidContainerRegistry.isEmptyContainer(stack) => true
      case _ => false
    }
  }


  /**
   * Gets the fluid capacity of a container ItemStack
   * @param stack Stack to check.
   * @return Capacity of the container.
   */
  def getContainerCapacity(stack: ItemStack): Int = {
    Option(stack).withFilter(s => FluidUtil.isFluidContainer(s))
      .flatMap(s => Option(s.getItem))
      .map {
        case item: IFluidContainerItem => item.getCapacity(stack)
        case _ => FluidContainerRegistry.getContainerCapacity(stack)
      }.getOrElse(0)
  }

  /**
   * Attempts to fill a container with a fluid.
   * @param stackTmp ItemStack of container.
   * @param fluidTmp FluidStack to fill into container.
   * @return Filled container and Remaining fluid not filled. None if all has been filled into container.
   */
  def fillFluidContainer(stackTmp: ItemStack, fluidTmp: FluidStack): (Option[ItemStack], Option[FluidStack]) = {
    type output = (Option[ItemStack], Option[FluidStack])
    val stack = Option(stackTmp)
    val fluid = Option(fluidTmp)

    (for {
      s <- stack.filter(s2 => canFillContainer(s2)).map(_.copy())
      f <- fluid
    } yield {
      Option(s.getItem).map[output] {
        case item: IFluidContainerItem =>
          val filled = item.fill(s, f, true)
          (Option(s), fluid.withFilter(_.amount > filled)
            .map(f => new FluidStack(f, f.amount - filled)))
        case _ =>
          val capacity = getContainerCapacity(s)
          (Option(FluidContainerRegistry.fillFluidContainer(f, s)),
          if (f.amount > capacity) Option(new FluidStack(f,  f.amount - capacity))
          else None)
      }
    }).flatten.getOrElse((stack, fluid))
  }

  /**
   * Drains the contents of a fluid container.
   * @param stackTmp ItemStack representing a container.
   * @param maxDrain Amount of fluid to drain
   * @return The drained container and the fluid actually drained. None if none is drained.
   */
  def drainFluidContainer(stackTmp: ItemStack, maxDrain: Int): (Option[ItemStack], Option[FluidStack]) = {
    type output = (Option[ItemStack], Option[FluidStack])
    val stack = Option(stackTmp)
    if (maxDrain < 0)
      return (stack, None)
    stack.flatMap(s => Option(s.getItem)).flatMap[output] {
      //Handle IFluidContainerItem
      case item: IFluidContainerItem if stack.exists(s => !FluidUtil.isEmptyFluidContainer(s)) =>
        val changedStack = stack.map(_.copy())
        val drained = changedStack.map(s => item.drain(s, maxDrain, true))
        drained.filter(_.amount > 0).map(f => (changedStack, Option(f)))

      //Handle basic containers.
      case _ if stack.exists(s => FluidContainerRegistry.isFilledContainer(s)) =>
        val drained = stack.map( s => FluidContainerRegistry.getFluidForFilledItem(s))
        val changedStack = stack.map(s => FluidContainerRegistry.drainFluidContainer(s))
        //Failed to drain or container capacity is more then maxDrain parameter.
        if (changedStack.isEmpty || drained.exists(_.amount > maxDrain))
          None
        else
          Option((changedStack, drained))

      case _ => None
    }.getOrElse((stack, None))
  }

  /**
   * Gets a Placeholder ItemStack
   */

  def getFluidPlaceholder(fluid: Fluid): Option[ItemStack] = {
    if (!FluidRegistry.isFluidRegistered(fluid))
      return None
    val nbt = new NBTTagCompound
    nbt.setString("fluidName" ,fluid.getName)
    val stack = new ItemStack(ItemEnum.FLUIDPLACEHOLDER.getItem, 1)
    stack.setTagCompound(nbt)
    Option(stack)
  }

  /**
   * Gets a [[Fluid]] from a Placeholder ItemStack
   *
   * @param stack Placeholder ItemStack
   */

  def getFluidFromPlaceholder(stack: ItemStack): Option[Fluid] = {
    Option(stack)
      .filter(s => s.getItem == ItemEnum.FLUIDPLACEHOLDER.getItem && s.hasTagCompound)
      .map(s => FluidRegistry.getFluid(s.getTagCompound.getString("fluidName")))
  }

}
