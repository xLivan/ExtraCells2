package extracells.core.client.gui

import appeng.api.storage.data.IAEFluidStack
import extracells.core.client.gui.widget.TFluidSelectorGui
import extracells.core.common.container.implementations.ContainerFluidStorage
import extracells.core.common.network.NetworkWrapper
import extracells.core.common.network.packet.PacketFluidStorage
import extracells.core.common.registries.GuiEnum
import extracells.core.common.util.TFluidSelector
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiTextField
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11

import scala.collection.mutable.IndexedSeq
//Todo: Implement
class GuiFluidStorage(player: EntityPlayer, val guiName: String, val container: ContainerFluidStorage)
  extends GuiContainer(container) with TFluidSelectorGui{

  //val fluidWidgets: IndexedSeq[]

  var searchBox: GuiTextField = _

  this.xSize = 176
  this.ySize = 204
  this.container.setGuiContainer(this)

  override def initGui(): Unit = {
    super.initGui()
    updateFluids()
    //Collections.sort(this.fluidWidgets, new FluidWidgetComparator());

    this.searchBox = new GuiTextField(this.fontRendererObj,
      this.guiLeft + 81, this.guiTop + 6, 88, 10) {

      override def mouseClicked(x: Int, y: Int, mouseBtn: Int): Unit = {
        val flag: Boolean =  Range(this.xPosition, this.xPosition + this.width).contains(x) &&
          Range(this.yPosition, this.yPosition + this.height).contains(y)
        if (flag && mouseBtn == 3)
          setText("")
      }
    }

    this.searchBox.setEnableBackgroundDrawing(true)
    this.searchBox.setFocused(true)
    this.searchBox.setMaxStringLength(15)
  }

  override def drawGuiContainerBackgroundLayer(partialTicks: Float,
                                               mouseX: Int, mouseY: Int): Unit = {
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F)
    Minecraft.getMinecraft.renderEngine.bindTexture(GuiEnum.FluidTerm.getTexture)
    drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize)
    this.searchBox.drawTextBox()
    NetworkWrapper.sendToServer(new PacketFluidStorage())
  }

  override def getCurrentFluid: IAEFluidStack = null

  override def getSelector: TFluidSelector = null

  override def getGuiLeft: Int = this.guiLeft

  override def getGuiTop: Int = this.guiTop

  def updateFluids(): Unit = {

  }
}
