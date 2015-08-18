package extracells.common.networking

import java.util

import appeng.api.networking._
import appeng.api.networking.storage.IStorageGrid
import appeng.api.parts.PartItemStack
import appeng.api.storage.IMEMonitor
import appeng.api.storage.data.IAEFluidStack
import appeng.api.util.{DimensionalCoord, AEColor}
import extracells.common.part.PartECBase
import net.minecraft.item.ItemStack
import net.minecraftforge.common.util.ForgeDirection

class ECGridProxy(protected var host: PartECBase) extends IGridBlock {
  protected var color: AEColor = _
  protected var grid: IGrid = _
  protected var usedChannels: Int = _

  override def getFlags: util.EnumSet[GridFlags] = util.EnumSet.of(GridFlags.REQUIRE_CHANNEL)
  override def getConnectableSides: util.EnumSet[ForgeDirection] = util.EnumSet.noneOf(classOf[ForgeDirection])

  override def isWorldAccessible = false

  override def getMachine: IGridHost = host
  override def getIdlePowerUsage: Double = host.getPowerUsage
  override def getLocation: DimensionalCoord = host.getLocation

  override def getGridColor: AEColor = if(color == null) AEColor.Transparent else color
  override def getMachineRepresentation: ItemStack = host.getItemStack(PartItemStack.Network)

  override def onGridNotification(notification: GridNotification) {}
  override def gridChanged() {}

  override def setNetworkStatus(grid: IGrid, usedChannels: Int) {
    this.grid = grid
    this.usedChannels = usedChannels
  }

  @throws(classOf[ECGridException])
  def getGridNode: IGridNode = {
    if (host == null)
      throw new ECGridException
    val node = host.getGridNode
    if (node == null)
      throw new ECGridException
    return node
  }

  @throws(classOf[ECGridException])
  def getGrid: IGrid = {
    val grid = getGridNode.getGrid
    if (grid == null)
      throw new ECGridException
    return grid
  }

  @throws(classOf[ECGridException])
  def getStorage: IStorageGrid = {
    val storageGrid = grid.getCache(classOf[IStorageGrid])
    if (storageGrid == null)
      throw new ECGridException
    return storageGrid
  }
}
