package extracells.common.util

import extracells.api.storage.filter.{FilterType, IFluidFilter}
import net.minecraftforge.fluids.Fluid

import scala.collection.mutable.ListBuffer


class BasicFluidFilter extends IFluidFilter{
  private val terminalBlacklist = new ListBuffer[Fluid]
  private val storageBlacklist = new ListBuffer[Fluid]
  BasicFluidFilter.instance = this

  override def isAllowed(filterType: FilterType, fluid: Fluid): Boolean = filterType match {
    case FilterType.TERMINAL => !terminalBlacklist.contains(fluid)
    case FilterType.STORAGE => !storageBlacklist.contains(fluid)
    case _ => true
  }
}

object BasicFluidFilter {
  private var instance: BasicFluidFilter = _

  def addTerminalBlacklist(fluid:Fluid) = instance.terminalBlacklist.append(fluid)

  def addStorageBlacklist(fluid: Fluid) = instance.storageBlacklist.append(fluid)
}