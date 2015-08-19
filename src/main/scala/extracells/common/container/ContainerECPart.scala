package extracells.common.container

import extracells.common.part.PartECBase
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.inventory.Container

class ContainerECPart(private val part: PartECBase, private val invPlayer: InventoryPlayer) extends Container {

  override def canInteractWith(player: EntityPlayer): Boolean = {
    return part.isValid
  }
}
