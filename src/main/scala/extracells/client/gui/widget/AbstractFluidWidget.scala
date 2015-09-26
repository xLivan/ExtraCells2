package extracells.client.gui.widget

import net.minecraft.client.gui.{FontRenderer, Gui}
import net.minecraft.client.renderer.RenderHelper
import net.minecraftforge.fluids.{FluidRegistry, Fluid}
import org.lwjgl.opengl.{GL12, GL11}

//TODO: Finish up class
abstract class AbstractFluidWidget(val fluidTermGui: TFluidWidgetGui,
                                    val height: Int, val width: Int,
                                    var fluid: Fluid) extends Gui{

  def setFluid(fluid: Fluid): Unit = {
    this.fluid = fluid
  }

  def setFluid(fluidID: Int): Unit = {
    this.fluid = FluidRegistry.getFluid(fluidID)
  }

  def getFluid: Fluid = this.fluid

  protected def isPointInRegion(top: Int, left: Int, height: Int, width: Int,
                                 pointX: Int, pointY: Int): Boolean = {
    val xPoint = pointX - this.fluidTermGui.getGuiLeft
    val yPoint = pointY - (this.fluidTermGui.getGuiTop + 18)


    ((top - 1) until (top + height + 1)).contains(xPoint) &&
      ((left - 1) until (left + width + 1)).contains(yPoint)
  }

  protected def drawHoverText(strs: Seq[String], x: Int, y: Int, fontRender: FontRenderer): Unit = {
    if (strs.isEmpty)
      return
    GL11.glDisable(GL12.GL_RESCALE_NORMAL)
    RenderHelper.disableStandardItemLighting()
    GL11.glDisable(GL11.GL_LIGHTING)
    GL11.glDisable(GL11.GL_DEPTH_TEST)
    var width = 0

    for (str: String <- strs) {
      val len = fontRender.getStringWidth(str)
      if (len > width)
        width = len
    }

    val i1 = x + 12
    var j1 = y - 12
    var k1 = 8

    if (strs.length > 1)
      k1 += (2 + (strs.length - 1) * 10)

    this.zLevel = 300.0F
    val l1 = -267386864
    val i2: Int = 1347420415
    val j2: Int = (i2 & 16711422) >> 1 | i2 & -16777216

    this.drawGradientRect(i1 - 3, j1 - 4, i1 + width + 3, j1 - 3, l1, l1)
    this.drawGradientRect(i1 - 3, j1 + k1 + 3, i1 + width + 3, j1 + k1 + 4, l1, l1)
    this.drawGradientRect(i1 - 3, j1 - 3, i1 + width + 3, j1 + k1 + 3, l1, l1)
    this.drawGradientRect(i1 - 4, j1 - 3, i1 - 3, j1 + k1 + 3, l1, l1)
    this.drawGradientRect(i1 + width + 3, j1 - 3, i1 + width + 4, j1 + k1 + 3, l1, l1)
    this.drawGradientRect(i1 - 3, j1 - 3 + 1, i1 - 3 + 1, j1 + k1 + 3 - 1, i2, j2)
    this.drawGradientRect(i1 + width + 2, j1 - 3 + 1, i1 + width + 3, j1 + k1 + 3 - 1, i2, j2)
    this.drawGradientRect(i1 - 3, j1 - 3, i1 + width + 3, j1 - 3 + 1, i2, i2)
    this.drawGradientRect(i1 - 3, j1 + k1 + 2, i1 + width + 3, j1 + k1 + 3, j2, j2)

    for (k2 <- strs.indices) {
      val str = strs(k2)
      fontRender.drawStringWithShadow(str, i1, j1, -1)

      if (k2 == 0)
        j1 += 2
      j1 += 10
    }

    this.zLevel = 0.0F
    GL11.glEnable(GL11.GL_LIGHTING)
    GL11.glEnable(GL11.GL_DEPTH_TEST)
    RenderHelper.enableStandardItemLighting()
    GL11.glEnable(GL12.GL_RESCALE_NORMAL)
  }

  def drawTooltip(posX: Int, posY: Int, mouseX: Int, mouseY: Int): Boolean
  def drawWidget(posX: Int, posY: Int): Unit
  def mouseClicked(posX: Int, posY: Int, mouseX: Int, mouseY: Int): Unit
}
