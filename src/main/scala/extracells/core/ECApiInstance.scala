package extracells.core


import extracells.api.storage.filter.{FilterType, IFluidFilter}
import extracells.core.common.util.BasicFluidFilter
import net.minecraftforge.fluids.Fluid

import scala.collection.mutable

object ECApiInstance extends extracells.api.IECApi {
  private val fluidFilters: mutable.Buffer[IFluidFilter] = new mutable.ListBuffer[IFluidFilter]

  override def getVersion = ExtraCells.VERSION

  override def registerFluidFilter(filter: IFluidFilter): Unit = {
    if (!fluidFilters.contains(filter))
      fluidFilters.append(filter)
  }

  override def registerFuelBurnTime(fuel: Fluid, burnTime: Int): Unit = ???

  override def isFluidAllowed(filterType: FilterType, fluid: Fluid): Boolean = {
    for (filter <- fluidFilters if !filter.isAllowed(filterType, fluid))
      return false
    BasicFluidFilter.instance.isAllowed(filterType, fluid)
  }
}
