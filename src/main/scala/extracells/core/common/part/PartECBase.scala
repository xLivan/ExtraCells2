package extracells.core.common.part

import java.io.IOException
import java.util

import appeng.api.AEApi
import appeng.api.implementations.IPowerChannelState
import appeng.api.networking.IGridNode
import appeng.api.networking.events.{MENetworkEventSubscribe, MENetworkPowerStatusChange}
import appeng.api.networking.security.IActionHost
import appeng.api.networking.ticking.IGridTickable
import appeng.api.parts._
import appeng.api.util.{AECableType, AEColor, DimensionalCoord}
import cpw.mods.fml.common.FMLCommonHandler
import cpw.mods.fml.relauncher.{Side, SideOnly}
import extracells.core.client.render.TextureEnum
import extracells.core.common.grid.{ECGridProxy, TGridProxyable}
import extracells.core.common.network.GuiHandler
import extracells.core.common.registries.{ItemEnum, PartEnum}
import io.netty.buffer.ByteBuf
import net.minecraft.client.renderer.{RenderBlocks, Tessellator}
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.entity.{Entity, EntityLivingBase}
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.{IIcon, Vec3}
import net.minecraftforge.common.util.ForgeDirection

abstract class PartECBase extends IPart with TGridProxyable with IActionHost with IPowerChannelState {
  protected var node: Option[IGridNode] = None
  protected var host: Option[IPartHost] = None
  protected var tile: Option[TileEntity] = None
  protected var hostTile: Option[TileEntity] = None
  protected var proxy: Option[ECGridProxy] = None
  protected var side: ForgeDirection = ForgeDirection.UNKNOWN
  protected var redstonePowered: Boolean = false
  protected var owner: Option[EntityPlayer] = None
  protected var powerUsage: Double = 0.0
  protected var sleeping = false

  var isActive = false
  var isPowered = false

  def addToWorld() {
    if (FMLCommonHandler.instance().getEffectiveSide.isClient)
      return
    this.proxy = Option(new ECGridProxy(this))
    this.node = this.proxy.map(pr => AEApi.instance.createGridNode(pr))
    for (n <- this.node) {
      this.owner.foreach(p =>
        n.setPlayerID(AEApi.instance.registries.players().getID(p)))
      n.updateState()
    }
  }

  def removeFromWorld(): Unit = {
    this.node.foreach(_.destroy())
  }

  def initializePart(partStack: ItemStack): Unit = {
    if (partStack.hasTagCompound)
      readFromNBT(partStack.getTagCompound)
  }

  def saveData(): Unit = {
    this.host.foreach(_.markForSave())
  }

  def wakePart(): Unit = {
    if (!this.isInstanceOf[IGridTickable] || !this.sleeping)
      return
    this.sleeping = false
    for {
      tm <- this.proxy.flatMap(_.getTickManager)
      gn <- this.node
    } tm.wakeDevice(gn)
  }

  def alertPart(): Unit = {
    if (!this.isInstanceOf[IGridTickable])
      return
    for {
      tm <- this.proxy.flatMap(_.getTickManager)
      gn <- this.node
    } tm.alertDevice(gn)
  }

  def sleepPart(): Unit = {
    if (!this.isInstanceOf[IGridTickable] || this.sleeping)
      return
    this.sleeping = true
    for {
      tm <- this.proxy.flatMap(_.getTickManager)
      gn <- this.node
    } tm.sleepDevice(gn)
  }

  override def securityBreak(): Unit = {


  }


  def isValid: Boolean = this.host.map(_.getPart(this.side)).exists(_ eq this)
  override def isProvidingWeakPower: Int = 0
  override def isProvidingStrongPower: Int = 0
  override def isLadder(entity: EntityLivingBase): Boolean = false
  override def isSolid: Boolean = false

  override def canBePlacedOn(what: BusSupport): Boolean = what != BusSupport.DENSE_CABLE

  override def canConnectRedstone: Boolean = false

  def getDrops(drops: util.List[ItemStack], wrenched: Boolean): Unit = {}

  def getCableConnectionType(dir: ForgeDirection) : AECableType = AECableType.GLASS

  def getBoxes(bch: IPartCollisionHelper): Unit

  def getBreakingTexture: IIcon = TextureEnum.BUS_SIDE.getTexture

  def getLightLevel: Int = 0

  def getSide: ForgeDirection = this.side

  def getPowerUsage: Double = this.powerUsage

  def getHostTile: Option[TileEntity] = this.hostTile

  def getTile: Option[TileEntity] = this.tile

  def getItemStack(partType: PartItemStack): ItemStack = {
    val stack = new ItemStack(ItemEnum.PARTITEM.getItem, 1, PartEnum.getPartID(this))
    val nbt = new NBTTagCompound
    partType match {
      case PartItemStack.Wrench => writeToNBT(nbt)
        stack.setTagCompound(nbt)
      case _ =>
    }
    stack
  }

  final override def getLocation: Option[DimensionalCoord] = this.tile map { tile =>
    new DimensionalCoord(tile.getWorld, tile.xCoord, tile.yCoord, tile.zCoord)}

  override def getGridNode: IGridNode = this.node.orNull

  override def getGridNode(dir: ForgeDirection): IGridNode = getGridNode

  def getHost: Option[IPartHost] = this.host

  final def getProxy: Option[ECGridProxy] = this.proxy

  final def getActionableNode: IGridNode = this.node.orNull

  final def getExternalFacingNode: IGridNode = null

  override def setPartHostInfo(side: ForgeDirection, host: IPartHost, tile: TileEntity): Unit = {
    this.side = side
    this.host = Option(host)
    this.tile = Option(tile)
  }

  @MENetworkEventSubscribe
  def onNetworkPowerStatusChange(e: MENetworkPowerStatusChange): Unit = {
    this.isActive = this.node.exists(_.isActive)
  }

  override def onPlacement(player: EntityPlayer,
                           held: ItemStack,
                           side: ForgeDirection): Unit = {
    this.owner = Option(player)
  }

  override def onActivate(player: EntityPlayer, pos: Vec3): Boolean = {
    for (h <- hostTile) player match {
      case player: EntityPlayerMP => GuiHandler.launchGui(GuiHandler.getSideOrID(this),
        player, h.getWorld, h.xCoord, h.yCoord, h.zCoord)
        return true
      case _ =>
    }
    false
  }

  override def onShiftActivate(player: EntityPlayer, pos: Vec3): Boolean = false
  override def onEntityCollision(entity: Entity): Unit = {}
  override def onNeighborChanged(): Unit = {}

  def gridChanged(): Unit = {}

  override def readFromNBT(data: NBTTagCompound): Unit = {
    if (data.hasKey("node")) {
      this.node foreach { n =>
        n.loadFromNBT("node0", data.getCompoundTag("node"))
        n.updateState()
      }
    }
  }

  override def writeToNBT(data: NBTTagCompound): Unit = {
    val nodeTag: NBTTagCompound = new NBTTagCompound
    this.node foreach {_.saveToNBT("node0", nodeTag)}
    data.setTag("node", nodeTag)
  }

  @throws(classOf[IOException])
  override def readFromStream(data: ByteBuf) : Boolean = {
    this.isActive = data.readBoolean()
    this.isPowered = data.readBoolean()
    true
  }

  @throws(classOf[IOException])
  override def writeToStream(data: ByteBuf) : Unit = {
    data.writeBoolean(this.node.exists(_.isActive))
    data.writeBoolean(this.isPowered)
  }

  //Renderer functions
  override def requireDynamicRender : Boolean = false

  @SideOnly(Side.CLIENT)
  def renderDynamic(x: Double, y: Double, z: Double,
                    rh: IPartRenderHelper, renderer: RenderBlocks) : Unit = {}

  @SideOnly(Side.CLIENT)
  def renderInventory(rh: IPartRenderHelper, renderer: RenderBlocks) : Unit

  @SideOnly(Side.CLIENT)
  def renderInventoryBusLights(rh: IPartRenderHelper, renderer: RenderBlocks) : Unit = {
    val ts: Tessellator = Tessellator.instance

    rh.setInvColor(0xFFFFFF)
    val otherIcon: IIcon = TextureEnum.BUS_COLOR.getTextures()(0)
    val side: IIcon = TextureEnum.BUS_SIDE.getTexture
    rh.setTexture(otherIcon, otherIcon, side, side, otherIcon, otherIcon)
    rh.renderInventoryBox(renderer)

    ts.setBrightness(13 << 20 | 13 << 4)
    rh.setInvColor(AEColor.Transparent.blackVariant)
    rh.renderInventoryFace(TextureEnum.BUS_COLOR.getTextures()(1), ForgeDirection.UP, renderer)
    rh.renderInventoryFace(TextureEnum.BUS_COLOR.getTextures()(1), ForgeDirection.DOWN, renderer)
    rh.renderInventoryFace(TextureEnum.BUS_COLOR.getTextures()(1), ForgeDirection.NORTH, renderer)
    rh.renderInventoryFace(TextureEnum.BUS_COLOR.getTextures()(1), ForgeDirection.EAST, renderer)
    rh.renderInventoryFace(TextureEnum.BUS_COLOR.getTextures()(1), ForgeDirection.SOUTH, renderer)
    rh.renderInventoryFace(TextureEnum.BUS_COLOR.getTextures()(1), ForgeDirection.WEST, renderer)
  }

  @SideOnly(Side.CLIENT)
  def renderStaticBusLights(x: Int, y: Int, z: Int, rh: IPartRenderHelper, renderer: RenderBlocks) {
    val ts: Tessellator = Tessellator.instance
    val otherIcon: IIcon = TextureEnum.BUS_COLOR.getTextures()(0)
    val side: IIcon = TextureEnum.BUS_SIDE.getTexture
    rh.setTexture(otherIcon, otherIcon, side, side, otherIcon, otherIcon)
    rh.renderBlock(x, y, z, renderer)
    if (isActive) {
      ts.setBrightness(13 << 20 | 13 << 4)
      ts.setColorOpaque_I(this.host.get.getColor.blackVariant)
    }
    else {
      ts.setColorOpaque_I(0x000000)
    }
    rh.renderFace(x, y, z, TextureEnum.BUS_COLOR.getTextures()(1), ForgeDirection.UP, renderer)
    rh.renderFace(x, y, z, TextureEnum.BUS_COLOR.getTextures()(1), ForgeDirection.DOWN, renderer)
    rh.renderFace(x, y, z, TextureEnum.BUS_COLOR.getTextures()(1), ForgeDirection.NORTH, renderer)
    rh.renderFace(x, y, z, TextureEnum.BUS_COLOR.getTextures()(1), ForgeDirection.EAST, renderer)
    rh.renderFace(x, y, z, TextureEnum.BUS_COLOR.getTextures()(1), ForgeDirection.SOUTH, renderer)
    rh.renderFace(x, y, z, TextureEnum.BUS_COLOR.getTextures()(1), ForgeDirection.WEST, renderer)
  }
}
