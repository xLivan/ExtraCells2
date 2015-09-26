package extracells.common.util

import appeng.api.definitions.IItemDefinition
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack

object InventoryUtil {
  /**
   * Search an [[IInventory]] for a [[IItemDefinition]]
   * @param inventory Inventory to search in.
   * @param itemDef IItemDefinition to look for
   * @return [[Option]] containing a [[ItemStack]] representing the search results.
   */
  def search(inventory: IInventory, itemDef: IItemDefinition): Option[ItemStack] = {
    val inv = Option(inventory)
    val output = Option(itemDef.maybeStack(0).orNull)
    if (inv.isEmpty || output.isEmpty)
      return None
    for (i <- 0 until inventory.getSizeInventory)
      Option(inventory.getStackInSlot(i))
        .filter(s1 => itemDef.isSameAs(s1))
        .foreach(s2 => output.foreach(_.stackSize += s2.stackSize))
    output.filter(_.stackSize > 0)
  }
}
