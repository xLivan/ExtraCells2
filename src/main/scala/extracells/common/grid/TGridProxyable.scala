package extracells.common.grid

import appeng.api.networking.IGridHost
import appeng.api.util.DimensionalCoord

trait TGridProxyable extends IGridHost{
  def getProxy : Option[ECGridProxy]

  def getLocation : Option[DimensionalCoord]

  def gridChanged() : Unit
}
