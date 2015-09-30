package extracells.core.common.item

import java.util.{List => JavaList}

import appeng.api.implementations.items.IStorageComponent
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.{EnumRarity, ItemStack, Item}
import net.minecraft.util.{MathHelper, IIcon}

import scala.collection.immutable

object ItemStorageComponent extends Item with IStorageComponent{
  val suffixes: immutable.Seq[String] = immutable.IndexedSeq("physical.256k", "physical.1024k", "physical.4096k",
    "physical.16384k", "fluid.1k", "fluid.4k", "fluid.16k",
    "fluid.64k", "fluid.256k", "fluid.1024k", "fluid.4096k")
  val spaces: immutable.Seq[Int] = immutable.IndexedSeq(262144, 1048576, 4194304, 16777216,
    1024, 4096, 16384, 65536, 262144, 1048576, 4194304)
  lazy val icons: Array[IIcon] = new Array(suffixes.size)

  setMaxDurability(0)
  setHasSubtypes(true)

  override def isStorageComponent(stack: ItemStack): Boolean = stack.getItem == this

  override def getBytes(stack: ItemStack): Int = if (stack.getMetadata > spaces.size) 0 else spaces(stack.getMetadata)
  override def getSubItems(item: Item, tab: CreativeTabs, list: JavaList[_]): Unit = {
    //Workaround for scala hating untyped generics
    for (i : Int <- suffixes.indices)
      list.asInstanceOf[JavaList[ItemStack]].add(new ItemStack(item, 1, i))
  }
  override def getUnlocalizedName(stack: ItemStack): String = {
    if (stack.getMetadata > suffixes.size)
      "extracells.invalidItem"
    else
      "extracells.item.storage.component." + suffixes(stack.getMetadata)
  }

  override def getIconFromDamage(dmg: Int): IIcon = this.icons(MathHelper.clamp_int(dmg, 0, this.suffixes.length))
  override def getRarity(stack: ItemStack): EnumRarity = if (stack.getMetadata >= 4)
    EnumRarity.rare else EnumRarity.epic

  override def registerIcons(iconReg: IIconRegister): Unit = {
    for (i <- this.suffixes.indices)
      this.icons(i) = iconReg.registerIcon("extracells:storage.component." + this.suffixes(i))
  }
}
