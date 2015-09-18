package extracells.common.part

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.entity.player.EntityPlayer

trait TGuiPart {
  @SideOnly(Side.CLIENT)
  def getServerGuiElement(entityPlayer: EntityPlayer) : AnyRef = null
  def getClientGuiElement(entityPlayer: EntityPlayer) : AnyRef = null
}
