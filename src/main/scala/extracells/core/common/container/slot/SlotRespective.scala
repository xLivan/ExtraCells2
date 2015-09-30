package extracells.core.common.container.slot

import net.minecraft.inventory.{IInventory, Slot}
import net.minecraft.item.ItemStack

/**
 * A slot that checks with the Inventory's isValidForSlot.
 * @param inventory Inventory the slot is used in.
 * @param index Slot Index
 * @param x Slot X
 * @param y Slot Y
 */
class SlotRespective(inventory: IInventory, index: Int, x: Int, y: Int) extends Slot(inventory, index, x, y) {
  override def isItemValid(stack: ItemStack): Boolean = this.inventory.isItemValidForSlot(this.slotNumber, stack)
}
