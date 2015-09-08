package extracells.client.gui

import appeng.api.storage.data.IAEFluidStack
import extracells.client.gui.widget.TFluidSelectorGui
import extracells.common.container.implementations.ContainerFluidStorage
import extracells.common.network.packet.PacketFluidStorage
import extracells.common.util.TFluidSelector
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.entity.player.EntityPlayer

class GuiFluidStorage(val player: EntityPlayer, val guiName: String) extends GuiContainer(new ContainerFluidStorage(player)) with TFluidSelectorGui{
  val container: ContainerFluidStorage = this.inventorySlots.asInstanceOf[ContainerFluidStorage]
  val xSize: Int = 176
  val ySize: Int = 204

  this.container.setGuiContainer(this)

  //new PacketFluidStorage
  override def drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int): Unit = {}

  override def getCurrentFluid: IAEFluidStack = null

  override def getSelector: TFluidSelector = null

  override def getGuiLeft: Int = 0

  override def getGuiTop: Int = 0
}
