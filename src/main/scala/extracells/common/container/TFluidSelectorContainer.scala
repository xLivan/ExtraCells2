package extracells.common.container

import net.minecraftforge.fluids.Fluid

trait TFluidSelectorContainer {
  def setSelectedFluid(fluid: Fluid) : Unit
}
