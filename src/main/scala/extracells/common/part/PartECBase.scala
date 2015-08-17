package extracells.common.part

import appeng.api.implementations.IPowerChannelState
import appeng.api.networking.security.IActionHost
import appeng.api.networking.{IGridHost, IGridNode}
import appeng.api.parts.{IPart, IPartHost}
import cpw.mods.fml.common.FMLCommonHandler
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.common.util.ForgeDirection

abstract class PartECBase extends IPart with IGridHost with IActionHost with IPowerChannelState {
  protected var node: IGridNode
  protected var side: ForgeDirection
  protected var host: IPartHost
  protected var tile: TileEntity
  protected var hostTile: TileEntity
  protected var redstonePowered: Boolean
  protected var isActive: Boolean
  protected var isPowered = false
  protected var owner: EntityPlayer
  protected var powerUsage: Double


  def addToWorld() {
    if(FMLCommonHandler.instance().getEffectiveSide.isClient)
      return

  }
}
