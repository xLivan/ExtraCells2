package extracells.common.item

import java.util
import java.util.{List => JavaList, Set => JavaSet}
import appeng.api.config.FuzzyMode
import cpw.mods.fml.relauncher.{Side, SideOnly}
import extracells.api.IFluidStorageCell
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.{EnumRarity, ItemStack, Item}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.{MathHelper, IIcon}
import net.minecraft.world.World
import net.minecraftforge.fluids.Fluid

class ItemStorageFluid extends ItemStorageBase with IFluidStorageCell{
  val suffixes: Array[String] = Array("1k", "4k", "16k", "64k", "256k", "1024k", "4096k")
  val spaces: Array[Int] = Array(1024, 4096, 16348, 65536, 262144, 1048576, 4194304)
  lazy val icons: Array[IIcon] = new Array[IIcon](suffixes.length)

  setMaxStackSize(1)
  setMaxDamage(0)
  setHasSubtypes(true)

  override def onItemRightClick(itemStack: ItemStack, world: World, player: EntityPlayer) : ItemStack = ???

  override def getRarity(itemStack: ItemStack) : EnumRarity = EnumRarity.rare
  override def getMaxTypes(itemStack: ItemStack) : Int = 5
  override def getMaxBytes(itemStack: ItemStack) : Int = spaces(Math.max(0,itemStack.getItemDamage))
  //TODO: Implement!
  override def getUpgradesInventory(itemStack: ItemStack) : IInventory = ???
  override def getConfigInventory(itemStack: ItemStack): IInventory = ???
  override def getFilter(is: ItemStack): util.ArrayList[Fluid] = ???

  //Client-sided stuff
  override def addInformation(itemStack: ItemStack, player: EntityPlayer, list: JavaList[_], bool: Boolean): Unit = {
    val strList: JavaList[String] = list.asInstanceOf[JavaList[String]]
    //TODO: Implement
  }
  override def registerIcons(iconRegister: IIconRegister): Unit = {
    for (i: Int <- 0 until suffixes.length)
      this.icons(i) = iconRegister.registerIcon("extracells:" + "storage.fluid" + suffixes(i))
  }

  override def getUnlocalizedName(itemStack: ItemStack) : String = ???
  override def getIconFromDamage(dmg: Int) : IIcon =
    this.icons(MathHelper.clamp_int(dmg, 0, suffixes.length))
  override def getSubItems(item: Item, tab: CreativeTabs, list: JavaList[_]): Unit = {
    //Workaround for scala hating untyped generics
    for (i : Int <- 0 until suffixes.length)
      list.asInstanceOf[JavaList[ItemStack]].add(new ItemStack(item, 1, i))
  }



}
