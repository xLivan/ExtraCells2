package extracells.common.container

import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.inventory.Container
import net.minecraft.tileentity.TileEntity

class ContainerECBlock(private val tile: TileEntity, private val invPlayer: InventoryPlayer) extends ContainerECBase{

  override def canInteractWith(player:EntityPlayer ) : Boolean = {
    return tile.getWorld.getTileEntity(tile.xCoord, tile.yCoord, tile.zCoord).equals(tile)
  }

}
