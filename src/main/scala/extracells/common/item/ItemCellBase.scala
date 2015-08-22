package extracells.common.item

import appeng.api.config.FuzzyMode
import appeng.api.storage.ICellWorkbenchItem
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound

abstract class ItemCellBase extends Item with ICellWorkbenchItem{
  override def isEditable(itemStack: ItemStack): Boolean = if (itemStack == null) false
  else itemStack.getItem == this

  override def getFuzzyMode(itemStack: ItemStack): FuzzyMode = {
    if (itemStack == null)
      return null
    if (!itemStack.hasTagCompound || !itemStack.getTagCompound.hasKey("fuzzyMode"))
      setFuzzyMode(itemStack, FuzzyMode.IGNORE_ALL)
    FuzzyMode.valueOf(itemStack.getTagCompound.getString("fuzzyMode"))
  }

  override def setFuzzyMode(itemStack: ItemStack, fuzzy: FuzzyMode): Unit = {
    if(itemStack == null)
      return
    val tag: NBTTagCompound = if (itemStack.hasTagCompound) itemStack.getTagCompound
    else new NBTTagCompound

    tag.setString("fuzzyMode", fuzzy.name())
    itemStack.setTagCompound(tag)
  }
}
