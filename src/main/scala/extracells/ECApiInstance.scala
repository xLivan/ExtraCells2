package extracells

import extracells.api.IECApi
import extracells.api.storage.filter.{FilterType, IFluidFilter}
import extracells.common.util.BasicFluidFilter
import net.minecraftforge.fluids.Fluid

import scala.collection.mutable

class ECApiInstance extends IECApi {
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

object ECApiInstance {
  val instance: ECApiInstance = new ECApiInstance
}
