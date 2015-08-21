package extracells.common.inventory

import appeng.api.AEApi
import appeng.api.definitions.IItemDefinition
import extracells.api.definitions.IItemDefinition
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack

class UpgradeCardInventory(name: String, size: Int, val acceptedCards: Option[Array[IItemDefinition]])
    extends ECInventoryBase(name, size, 1){

  override def isItemValidForSlot(index: Int, stack: ItemStack): Boolean = {
    val cards = AEApi.instance.definitions.materials().cardFuzzy()
  }
  override def isUseableByPlayer(player: EntityPlayer): Boolean = true
}
