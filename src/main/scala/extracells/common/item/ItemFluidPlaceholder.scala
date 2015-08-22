package extracells.common.item

import net.minecraft.item.{ItemStack, Item}
import net.minecraftforge.fluids.FluidRegistry

class ItemFluidPlaceholder extends Item {

  override def getItemStackDisplayName(stack: ItemStack): String = {
    val fluid = FluidRegistry.getFluid(stack.getMetadata)
    if (fluid == null || fluid.getBlock == null)
      return null
    val item = Item.getItemFromBlock(fluid.getBlock)
    if (item == null)
      return fluid.getName
    return item.getItemStackDisplayName(new ItemStack(item))
  }
}
