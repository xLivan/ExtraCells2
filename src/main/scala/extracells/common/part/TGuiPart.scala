package extracells.common.part

import cpw.mods.fml.relauncher.{Side, SideOnly}

trait TGuiPart {
  @SideOnly(Side.CLIENT)
  def getClientGuiElement: AnyRef
  def getServerGuiElement: AnyRef
}
