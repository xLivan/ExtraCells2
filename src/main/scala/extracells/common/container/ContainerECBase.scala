package extracells.common.container

import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.Container

abstract class ContainerECBase extends Container {
  protected var gui: GuiContainer = _
  def setGuiContainer(gui: GuiContainer): Unit ={
    this.gui = gui
  }
}
