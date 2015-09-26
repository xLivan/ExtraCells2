package extracells.common.util

import net.minecraftforge.fluids.{Fluid, FluidStack}

object GasUtil {
  def isGas(stack: FluidStack): Boolean = stack.getFluid.isGaseous(stack)
}
