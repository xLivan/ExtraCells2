package extracells.common.item

import java.util.{List => JavaList}

import extracells.ExtraCells
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.{ItemStack, Item}
import net.minecraft.util.{MathHelper, IIcon}

import scala.collection.immutable

class ItemCellCasing extends Item {
  val suffixes: immutable.Seq[String] = immutable.IndexedSeq[String]("physical", "fluid")
  private lazy val icons: Array[IIcon] = new Array(suffixes.length)

  setMaxDurability(0)
  setHasSubtypes(true)
  setCreativeTab(ExtraCells.ModTab)

  override def getIconFromDamage(meta: Int): IIcon = this.icons(MathHelper.clamp_int(meta, 0, icons.length - 1))
  override def getSubItems(item: Item, tab: CreativeTabs, list: JavaList[_]): Unit = {
    //Workaround for scala hating untyped generics
    for (i : Int <- suffixes.indices)
      list.asInstanceOf[JavaList[ItemStack]].add(new ItemStack(item, 1, i))
  }
  override def getUnlocalizedName(stack: ItemStack): String = {
    //To prevent some itemStack with invalid meta causing crash.
    "extracells.item.storage.casing.".concat(
      if (suffixes.indices.contains(stack.getMetadata))
        suffixes(stack.getMetadata)
      else
        "unknown"
    )
  }
  override def registerIcons(iconReg: IIconRegister): Unit = {
    for (i <- this.suffixes.indices)
      this.icons(i) = iconReg.registerIcon("extracells:storage.casing."
        .concat(suffixes(i)))
  }
}
