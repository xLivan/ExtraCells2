package extracells.common.container

import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.Container

/**
 * Base container class.
 * @tparam GUIClass Class that extends GuiContainer, GuiContainer used by this container.
 */
abstract class ContainerECBase[GUIClass >: GuiContainer] extends Container {
  protected var gui: Option[GUIClass] = None

  def setGuiContainer(gui: GUIClass): Unit = this.gui = Option(gui)

  def getGuiContainer: Option[GUIClass] = this.gui
}
