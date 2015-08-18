package extracells.common.grid

import appeng.api.networking.IGridHost
import appeng.api.util.DimensionalCoord

trait TGridProxyable extends IGridHost{
  def getProxy : ECGridProxy

  def getLocation : DimensionalCoord

  def gridChanged : Unit
}
