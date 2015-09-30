package extracells.core.common.container

import extracells.core.common.part.PartECBase
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.inventory.Container

class ContainerECPart(private val part: PartECBase, private val invPlayer: InventoryPlayer) extends ContainerECBase {

  override def canInteractWith(player: EntityPlayer): Boolean = {
    return part.isValid
  }
}
