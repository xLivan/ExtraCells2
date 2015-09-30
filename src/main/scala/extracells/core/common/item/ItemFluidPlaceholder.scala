package extracells.core.common.item

import net.minecraft.item.{Item, ItemStack}
import net.minecraftforge.fluids.{FluidRegistry, FluidStack}

object ItemFluidPlaceholder extends Item {

  override def getItemStackDisplayName(stack: ItemStack): String = {
    if (!stack.hasTagCompound)
      return "errorNoTagCompound"
    val fluidName = stack.getTagCompound.getString("fluidName")
    val fluid = FluidRegistry.getFluid(fluidName)
    if (fluid eq null)
      return "errorNoSuchFluid: ".concat(fluidName)
    fluid.getLocalizedName(new FluidStack(fluid, 0))
  }
}
