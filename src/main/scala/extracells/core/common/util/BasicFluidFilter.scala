package extracells.core.common.util

import extracells.api.storage.filter.{FilterType, IFluidFilter}
import extracells.api.storage.filter.FilterType
import net.minecraftforge.fluids.{FluidRegistry, Fluid}

import scala.collection.mutable.ListBuffer

/**
 * Basic name matching blacklist filter.
 * Always runs last.
 */
class BasicFluidFilter extends IFluidFilter{
  private val terminalBlacklist = new ListBuffer[Fluid]
  private val storageBlacklist = new ListBuffer[Fluid]

  override def isAllowed(filterType: FilterType, fluid: Fluid): Boolean = {
    //So multiple Fluids with the same name will be matched.
    val fluidReg = FluidRegistry.getFluid(fluid.getName)
    filterType match {
      case FilterType.TERMINAL => !terminalBlacklist.contains(fluidReg)
      case FilterType.STORAGE => !storageBlacklist.contains(fluidReg)
      case _ => true
    }
  }
}

object BasicFluidFilter {
  val instance: BasicFluidFilter = new BasicFluidFilter

  def addTerminalBlacklist(fluid:Fluid) = instance.terminalBlacklist.append(fluid)

  def addStorageBlacklist(fluid: Fluid) = instance.storageBlacklist.append(fluid)
}