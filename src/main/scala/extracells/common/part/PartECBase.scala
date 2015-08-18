package extracells.common.part

import java.io.IOException
import java.util

import appeng.api.AEApi
import appeng.api.implementations.IPowerChannelState
import appeng.api.networking.events.{MENetworkPowerStatusChange, MENetworkEventSubscribe}
import appeng.api.networking.security.IActionHost
import appeng.api.networking.IGridNode
import appeng.api.parts._
import appeng.api.util.{AECableType, AEColor, DimensionalCoord}
import cpw.mods.fml.common.FMLCommonHandler
import cpw.mods.fml.relauncher.{Side, SideOnly}
import extracells.common.grid.{TGridProxyable, ECGridProxy}
import extracells.client.render.TextureManager
import io.netty.buffer.ByteBuf
import net.minecraft.client.renderer.{Tessellator, RenderBlocks}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.IIcon
import net.minecraftforge.common.util.ForgeDirection

abstract class PartECBase extends IPart with TGridProxyable with IActionHost with IPowerChannelState {
  protected var node: IGridNode = _
  protected var host: IPartHost = _
  protected var tile: TileEntity = _
  protected var hostTile: TileEntity = _
  protected var proxy: ECGridProxy = _
  protected var side: ForgeDirection = _
  protected var redstonePowered: Boolean = _
  protected var isActive: Boolean = _
  protected var isPowered: Boolean = false
  protected var owner: EntityPlayer = _
  protected var powerUsage: Double = _


  def addToWorld() {
    if (FMLCommonHandler.instance().getEffectiveSide.isClient)
      return
    this.proxy = new ECGridProxy(this)
    this.node = AEApi.instance.createGridNode(this.proxy)
    if (this.node != null) {
      if (this.owner != null)
        this.node.setPlayerID(AEApi.instance.registries.players().getID(this.owner))
      this.node.updateState()
    }

  }

  def removeFromWorld(): Unit = {
    if (this.node == null)
      return
    this.node.destroy()
  }

  def initializePart(partStack: ItemStack): Unit = {
    if (partStack.hasTagCompound)
      readFromNBT(partStack.getTagCompound)
  }

  def saveData(): Unit = {
    if (this.host == null)
      return
    this.host.markForSave()
  }

  override def securityBreak(): Unit = {

  }


  def isValid: Boolean = this.host.getPart(this.side).equals(this)

  override def canBePlacedOn(what: BusSupport): Boolean = what != BusSupport.DENSE_CABLE

  override def canConnectRedstone: Boolean = false

  def getDrops(drops: util.List[ItemStack], wrenched: Boolean): Unit

  def getCableConnectionType(dir: ForgeDirection) : AECableType = AECableType.SMART

  def getBoxes(bch: IPartCollisionHelper): Unit

  def getBreakingTexture: IIcon

  def getLightLevel: Int = 0

  def getSide: ForgeDirection = this.side

  def getPowerUsage: Double = this.powerUsage

  def getHostTile: TileEntity = this.hostTile

  def getTile: TileEntity = this.tile

  def getItemStack(partType: PartItemStack): ItemStack = {
    //TODO: Implement function
    return null
  }

  final def getLocation : DimensionalCoord = new DimensionalCoord(this.tile.getWorldObj,
    this.tile.xCoord, this.tile.yCoord, this.tile.zCoord)

  def getNode : IGridNode = this.node

  def getNode(dir: ForgeDirection) : IGridNode = this.node

  def getHost : IPartHost = this.host

  final def getProxy : ECGridProxy = this.proxy

  final def getActionableNode : IGridNode = this.node

  final def getExternalFacingNode : IGridNode = null

  def getServerGuiElement(entityPlayer: EntityPlayer) : Object = null

  def getClientGuiElement(entityPlayer: EntityPlayer) : Object = null

  override def cableConnectionRenderTo() : Int

  @MENetworkEventSubscribe
  def onNetworkPowerStatusChange(e: MENetworkPowerStatusChange) : Unit = {
    if (this.node == null)
      return
    this.isActive = this.node.isActive
  }

  override def readFromNBT(data: NBTTagCompound) : Unit = {
    if (data.hasKey("node") && this.node != null) {
      this.node.loadFromNBT("node0", data.getCompoundTag("node"))
      this.node.updateState()
    }
  }

  override def writeToNBT(data: NBTTagCompound) : Unit = {
    if (this.node == null)
      return
    val nodeTag: NBTTagCompound = new NBTTagCompound
    this.node.saveToNBT("node0", nodeTag)
    data.setTag("node", nodeTag)
  }


  @throws(classOf[IOException])
  override def readFromStream(data: ByteBuf) : Boolean = {
    this.isActive = data.readBoolean()
    this.isPowered = data.readBoolean()
    return true;
  }

  @throws(classOf[IOException])
  override def writeToStream(data: ByteBuf) : Unit = {
    data.writeBoolean(this.node != null && this.node.isActive)
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
    val otherIcon: IIcon = TextureManager.BUS_COLOR.getTextures(0)
    val side: IIcon = TextureManager.BUS_SIDE.getTexture
    rh.setTexture(otherIcon, otherIcon, side, side, otherIcon, otherIcon)
    rh.renderInventoryBox(renderer)

    ts.setBrightness(13 << 20 | 13 << 4)
    rh.setInvColor(AEColor.Transparent.blackVariant)
    rh.renderInventoryFace(TextureManager.BUS_COLOR.getTextures()(1), ForgeDirection.UP, renderer)
    rh.renderInventoryFace(TextureManager.BUS_COLOR.getTextures()(1), ForgeDirection.DOWN, renderer)
    rh.renderInventoryFace(TextureManager.BUS_COLOR.getTextures()(1), ForgeDirection.NORTH, renderer)
    rh.renderInventoryFace(TextureManager.BUS_COLOR.getTextures()(1), ForgeDirection.EAST, renderer)
    rh.renderInventoryFace(TextureManager.BUS_COLOR.getTextures()(1), ForgeDirection.SOUTH, renderer)
    rh.renderInventoryFace(TextureManager.BUS_COLOR.getTextures()(1), ForgeDirection.WEST, renderer)
  }

}
