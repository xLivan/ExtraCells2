package extracells.common.util

import extracells.api.storage.filter.{FilterType, IFluidFilter}
import net.minecraftforge.fluids.Fluid

import scala.collection.mutable.ListBuffer


class FluidFilter extends IFluidFilter{
  private val terminalBlacklist = new ListBuffer[Fluid]
  private val storageBlacklist = new ListBuffer[Fluid]
  FluidFilter.instance = this

  override def isAllowed(filterType: FilterType, fluid: Fluid): Boolean = filterType match {
    case FilterType.TERMINAL => !terminalBlacklist.contains(fluid)
    case FilterType.STORAGE => !storageBlacklist.contains(fluid)
    case _ => true
  }
}

object FluidFilter {
  private var instance: FluidFilter = _

  def addTerminalBlacklist(fluid:Fluid) = instance.terminalBlacklist.append(fluid)

  def addStorageBlacklist(fluid: Fluid) = instance.storageBlacklist.append(fluid)
}