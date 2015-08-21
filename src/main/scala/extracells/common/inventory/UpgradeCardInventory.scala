package extracells.common.inventory

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack

class UpgradeCardInventory(name: String, size: Int)
    extends ECInventoryBase(name, size, 1){

  override def isItemValidForSlot(index: Int, stack: ItemStack): Boolean = ???
  override def isUseableByPlayer(player: EntityPlayer): Boolean = true
}
