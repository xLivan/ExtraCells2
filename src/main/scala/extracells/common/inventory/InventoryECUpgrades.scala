package extracells.common.inventory

import appeng.api.definitions.IItemDefinition
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraftforge.common.util.Constants

class InventoryECUpgrades(name: String, size: Int, val itemStack: ItemStack, val acceptedCards: Option[Set[IItemDefinition]] = None)
    extends ECInventoryBase(name, size, 1){
  if (itemStack.hasTagCompound)
    readFromNBT(itemStack.getTagCompound.getTagList("upgrades", Constants.NBT.TAG_COMPOUND))
  override def isItemValidForSlot(index: Int, stack: ItemStack): Boolean = {
    if (acceptedCards.isEmpty)
      return true
    for (itemDef <- acceptedCards.get if itemDef.isSameAs(stack))
      return true
    false
  }
  override def isUseableByPlayer(player: EntityPlayer) = true
}
