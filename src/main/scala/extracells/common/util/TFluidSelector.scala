package extracells.common.util

import net.minecraftforge.fluids.Fluid

trait TFluidSelector {
  def setSelectedFluid(fluid: Fluid) : Unit
}
