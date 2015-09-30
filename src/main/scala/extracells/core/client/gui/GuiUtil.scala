package extracells.core.client.gui

import net.minecraft.client.renderer.{Tessellator, OpenGlHelper}
import net.minecraft.util.IIcon
import org.lwjgl.opengl.GL11

object GuiUtil {
  def drawGradientRect(zLevel: Float,
                       par1: Int, par2: Int, par3: Int,
                       par4: Int, par5: Int, par6: Int) {
    val f: Float = (par5 >> 24 & 255) / 255.0F
    val f1: Float = (par5 >> 16 & 255) / 255.0F
    val f2: Float = (par5 >> 8 & 255) / 255.0F
    val f3: Float = (par5 & 255) / 255.0F
    val f4: Float = (par6 >> 24 & 255) / 255.0F
    val f5: Float = (par6 >> 16 & 255) / 255.0F
    val f6: Float = (par6 >> 8 & 255) / 255.0F
    val f7: Float = (par6 & 255) / 255.0F
    GL11.glDisable(GL11.GL_TEXTURE_2D)
    GL11.glEnable(GL11.GL_BLEND)
    GL11.glDisable(GL11.GL_ALPHA_TEST)
    OpenGlHelper.glBlendFunc(770, 771, 1, 0)
    GL11.glShadeModel(GL11.GL_SMOOTH)
    val tessellator: Tessellator = Tessellator.instance
    tessellator.startDrawingQuads
    tessellator.setColorRGBA_F(f1, f2, f3, f)
    tessellator.addVertex(par3, par2, zLevel)
    tessellator.addVertex(par1, par2, zLevel)
    tessellator.setColorRGBA_F(f5, f6, f7, f4)
    tessellator.addVertex(par1, par4, zLevel)
    tessellator.addVertex(par3, par4, zLevel)
    tessellator.draw
    GL11.glShadeModel(GL11.GL_FLAT)
    GL11.glDisable(GL11.GL_BLEND)
    GL11.glEnable(GL11.GL_ALPHA_TEST)
    GL11.glEnable(GL11.GL_TEXTURE_2D)
  }

  def drawIcon(icon: IIcon, x: Int, y: Int, z: Int,
               width: Float, height: Float) {
    Option(icon) foreach { i =>
      val tessellator: Tessellator = Tessellator.instance
      tessellator.startDrawingQuads()
      tessellator.addVertexWithUV(x, y + height, z, i.getMinU, i.getMaxV)
      tessellator.addVertexWithUV(x + width, y + height, z, i.getMaxU, i.getMaxV)
      tessellator.addVertexWithUV(x + width, y, z, i.getMaxU, i.getMinV)
      tessellator.addVertexWithUV(x, y, z, i.getMinU, i.getMinV)
      tessellator.draw()
    }
  }

  def gridRangeCheck(xRange: Range, yRange: Range, x: Int, y: Int): Boolean = {
    (xRange contains x) && (yRange contains y)
  }

}
