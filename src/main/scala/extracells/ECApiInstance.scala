package extracells

import java.util.{EnumSet => JavaEnumSet}

import extracells.api.IECApi
import extracells.api.storage.filter.{FilterType, IFluidFilter}
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

  override def isFluidAllowed(filterTypes: JavaEnumSet[FilterType], fluid: Fluid): Boolean = {
    for (filter <- fluidFilters if !filter.isAllowed(filterTypes, fluid))
      return false
    true
  }
}

object ECApiInstance {
  val instance: ECApiInstance = new ECApiInstance
}
