package extracells.common.item

import extracells.common.registries.ItemEnum
import net.minecraft.item.{ItemStack, Item}
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.fluids.{Fluid, FluidStack, FluidRegistry}

class ItemFluidPlaceholder extends Item {

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
