package extracells.common.grid

import java.util

import appeng.api.config.SecurityPermissions
import appeng.api.networking._
import appeng.api.networking.security.ISecurityGrid
import appeng.api.networking.storage.IStorageGrid
import appeng.api.networking.ticking.ITickManager
import appeng.api.parts.PartItemStack
import appeng.api.util.{DimensionalCoord, AEColor}
import extracells.common.part.PartECBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraftforge.common.util.ForgeDirection

class ECGridProxy(protected var part: PartECBase) extends IGridBlock {
  protected var color: AEColor = _
  protected var usedChannels: Int = _

  private var grid: Option[IGrid] = None

  def hasPermission(player: EntityPlayer, perm: SecurityPermissions): Boolean = {
    (for {
      pl <- Option(player)
      se <- getSecurity
    } yield se.hasPermission(pl, perm))
    .getOrElse(true)
  }

  override def getFlags: util.EnumSet[GridFlags] = util.EnumSet.of(GridFlags.REQUIRE_CHANNEL)
  override def getConnectableSides: util.EnumSet[ForgeDirection] = util.EnumSet.noneOf(classOf[ForgeDirection])

  override def isWorldAccessible = false

  override def getMachine: IGridHost = part
  override def getIdlePowerUsage: Double = part.getPowerUsage
  override def getLocation: DimensionalCoord = part.getLocation.orNull

  override def getGridColor: AEColor = if(color == null) AEColor.Transparent else color
  override def getMachineRepresentation: ItemStack = part.getItemStack(PartItemStack.Network)

  override def onGridNotification(notification: GridNotification) {}
  override def gridChanged(): Unit = {
    grid = Option(part.getGridNode.getGrid)
    part.gridChanged()
  }

  override def setNetworkStatus(grid: IGrid, usedChannels: Int) {
    this.grid = Option(grid)
    this.usedChannels = usedChannels
  }

  @throws(classOf[ECGridException])
  def getGridNode: IGridNode = {
    if (part eq null)
      throw new ECGridException
    val node = part.getGridNode
    if (node eq null)
      throw new ECGridException
    node
  }

  def getGrid: Option[IGrid] = grid

  def getStorage: Option[IStorageGrid] = grid.map[IStorageGrid](_.getCache(classOf[IStorageGrid]))

  def getTickManager: Option[ITickManager] = grid.map[ITickManager](_.getCache(classOf[ITickManager]))

  def getSecurity: Option[ISecurityGrid] = grid.map[ISecurityGrid](_.getCache(classOf[ISecurityGrid]))
}
