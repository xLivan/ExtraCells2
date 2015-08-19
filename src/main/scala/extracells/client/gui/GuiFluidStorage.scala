package extracells.client.gui

import extracells.client.gui.widget.TFluidSelectorGui
import extracells.common.container.implementations.ContainerFluidStorage
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.Container


class GuiFluidStorage(val player: EntityPlayer, val guiName: String) extends GuiContainer(new ContainerFluidStorage) with TFluidSelectorGui{
  val container: ContainerFluidStorage = this.inventorySlots.asInstanceOf[ContainerFluidStorage]
  val xSize: Int = 176
  val ySize: Int = 204

  this.container.setGuiContainer(this)
  new PacketFluidStorage
}
