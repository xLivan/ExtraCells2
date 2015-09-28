package extracells.common.part

import java.util.Random

import appeng.api.config.SecurityPermissions
import appeng.api.networking.IGridNode
import appeng.api.networking.ticking.{IGridTickable, TickRateModulation, TickingRequest}
import appeng.api.parts.{IPartCollisionHelper, IPartHost, IPartRenderHelper}
import appeng.api.util.AEColor
import cpw.mods.fml.relauncher.{Side, SideOnly}
import extracells.client.render.TextureManager
import extracells.common.container.implementations.ContainerFluidTerminal
import net.minecraft.client.renderer.{RenderBlocks, Tessellator}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.{IIcon, Vec3}
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection

import scala.collection.mutable

//TODO: Implement
class PartFluidTerminal extends PartECFluidBase
    with IGridTickable
    with TGuiPart {

  val tickRate: (Int, Int) = (1, 20)
  val containers: mutable.ListBuffer[ContainerFluidTerminal] = mutable.ListBuffer()

  override def cableConnectionRenderTo(): Int = 1

  override def randomDisplayTick(world: World,
                                 x: Int, y: Int, z: Int,
                                 r: Random): Unit = {}

  override def getTickingRequest(node: IGridNode): TickingRequest = {
    this.sleeping = !hasWork
    new TickingRequest(tickRate._1, tickRate._2, this.sleeping, true)
  }

  override def tickingRequest(node: IGridNode, TicksSinceLastCall: Int): TickRateModulation = {
    if (doWork() && hasWork) {
      this.sleeping = false
      TickRateModulation.URGENT
    }
    else {
      this.sleeping = true
      TickRateModulation.SLEEP
    }
  }

  def hasWork: Boolean = containers.exists(_.hasWork)

  /**
   *  Tick all containers.
   *  @return If it managed to do any work.
   */
  def doWork(): Boolean = (for (con <- this.containers)
    yield con.doWork())
    .contains(true)

  override def getBoxes(bch: IPartCollisionHelper): Unit = {
    bch.addBox(2, 2, 14, 14, 14, 16)
    bch.addBox(4, 4, 13, 12, 12, 14)
    bch.addBox(5, 5, 12, 11, 11, 13)
  }

  override def onActivate(player: EntityPlayer, pos: Vec3): Boolean = {
    if (isActive &&
        getProxy.exists(_.hasPermission(player, SecurityPermissions.INJECT)) &&
        getProxy.exists(_.hasPermission(player, SecurityPermissions.EXTRACT))) {
      return super.onActivate(player, pos)
    }
    false
  }

  @SideOnly(Side.CLIENT)
  override def renderInventory(rh: IPartRenderHelper, renderer: RenderBlocks): Unit = {
    val ts: Tessellator = Tessellator.instance

    val side: IIcon = TextureManager.TERMINAL_SIDE.getTexture
    rh.setTexture(side)
    rh.setBounds(4, 4, 13, 12, 12, 14)
    rh.renderInventoryBox(renderer)
    rh.setTexture(side, side, side, TextureManager.BUS_BORDER.getTexture, side, side)
    rh.setBounds(2, 2, 14, 14, 14, 16)
    rh.renderInventoryBox(renderer)

    ts.setBrightness(13 << 20 | 13 << 4)

    rh.setInvColor(0xFFFFFF)
    rh.renderInventoryFace(TextureManager.BUS_BORDER.getTexture, ForgeDirection.SOUTH, renderer)

    rh.setBounds(3, 3, 15, 13, 13, 16)
    rh.setInvColor(AEColor.Transparent.blackVariant)
    rh.renderInventoryFace(TextureManager.TERMINAL_FRONT.getTextures()(0), ForgeDirection.SOUTH, renderer)
    rh.setInvColor(AEColor.Transparent.mediumVariant)
    rh.renderInventoryFace(TextureManager.TERMINAL_FRONT.getTextures()(1), ForgeDirection.SOUTH, renderer)
    rh.setInvColor(AEColor.Transparent.whiteVariant)
    rh.renderInventoryFace(TextureManager.TERMINAL_FRONT.getTextures()(2), ForgeDirection.SOUTH, renderer)

    rh.setBounds(5, 5, 12, 11, 11, 13)
    renderInventoryBusLights(rh, renderer)
  }

  @SideOnly(Side.CLIENT)
  override def renderStatic(x: Int, y: Int, z: Int, rh: IPartRenderHelper, renderer: RenderBlocks): Unit = {
    val ts: Tessellator = Tessellator.instance

    val side: IIcon = TextureManager.TERMINAL_SIDE.getTexture
    rh.setTexture(side)
    rh.setBounds(4, 4, 13, 12, 12, 14)
    rh.renderBlock(x, y, z, renderer)
    rh.setTexture(side, side, side, TextureManager.BUS_BORDER.getTexture, side, side)
    rh.setBounds(2, 2, 14, 14, 14, 16)
    rh.renderBlock(x, y, z, renderer)

    if (isActive) Tessellator.instance.setBrightness(13 << 20 | 13 << 4)

    ts.setColorOpaque_I(0xFFFFFF)
    rh.renderFace(x, y, z, TextureManager.BUS_BORDER.getTexture, ForgeDirection.SOUTH, renderer)

    val host: IPartHost = getHost.get
    rh.setBounds(3, 3, 15, 13, 13, 16)
    ts.setColorOpaque_I(host.getColor.blackVariant)
    rh.renderFace(x, y, z, TextureManager.TERMINAL_FRONT.getTextures()(0), ForgeDirection.SOUTH, renderer)
    ts.setColorOpaque_I(host.getColor.mediumVariant)
    rh.renderFace(x, y, z, TextureManager.TERMINAL_FRONT.getTextures()(1), ForgeDirection.SOUTH, renderer)
    ts.setColorOpaque_I(host.getColor.whiteVariant)
    rh.renderFace(x, y, z, TextureManager.TERMINAL_FRONT.getTextures()(2), ForgeDirection.SOUTH, renderer)

    rh.setBounds(5, 5, 12, 11, 11, 13)
    renderStaticBusLights(x, y, z, rh, renderer)
  }


  override def getServerGuiElement(player: EntityPlayer): AnyRef = new ContainerFluidTerminal(this, player)

  override def getClientGuiElement(player: EntityPlayer): AnyRef = ???
}
