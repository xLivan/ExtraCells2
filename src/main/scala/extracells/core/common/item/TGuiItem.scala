package extracells.core.common.item

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack

trait TGuiItem {
  @SideOnly(Side.CLIENT)
  def getServerGuiElement(entityPlayer: EntityPlayer, stack: ItemStack) : AnyRef = null
  def getClientGuiElement(entityPlayer: EntityPlayer, stack: ItemStack) : AnyRef = null
}
