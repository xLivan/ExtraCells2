package extracells.common.container.slot

import extracells.common.container.implementations.ContainerFluidStorage
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.{IInventory, Slot}
import net.minecraft.item.ItemStack

class SlotPlayerInventory(inv: IInventory, val container: ContainerFluidStorage, index: Int, x: Int, y: Int) extends Slot(inv, index, x, y) {
  override def canTakeStack(player: EntityPlayer): Boolean = {
    if (player == null || this.container == null)
      return true
    val stack: ItemStack = player.getCurrentEquippedItem
    if (stack == null)
      return true
    if (stack == this.inventory.getStackInSlot(getSlotIndex))
      return false
    true
  }
}
