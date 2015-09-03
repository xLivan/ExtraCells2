package extracells.common.item

import java.util
import java.util.{List => JavaList}

import appeng.api.AEApi
import appeng.api.storage.StorageChannel
import extracells.api.storage.{IHandlerFluidStorage, IFluidStorageCell}
import extracells.common.inventory.{InventoryECFluidConfig, InventoryECUpgrades}
import extracells.common.registries.ItemEnum
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.{EnumRarity, Item, ItemStack}
import net.minecraft.util.{StatCollector, IIcon, MathHelper}
import net.minecraft.world.World
import net.minecraftforge.common.util.Constants
import net.minecraftforge.fluids.{FluidRegistry, Fluid}

import scala.collection.immutable

class ItemFluidCell extends ItemCellBase with IFluidStorageCell{
  val suffixes: immutable.Seq[String] = immutable.IndexedSeq("1k", "4k", "16k", "64k", "256k", "1024k", "4096k")
  val spaces: immutable.Seq[Int] = immutable.IndexedSeq(1024, 4096, 16348, 65536, 262144, 1048576, 4194304)
  lazy val icons: Array[IIcon] = new Array[IIcon](spaces.length)

  setMaxStackSize(1)
  setMaxDurability(0)
  setHasSubtypes(true)

  override def onItemRightClick(stack: ItemStack, world: World, player: EntityPlayer) : ItemStack = {
    if (!player.isSneaking)
      return stack

    val handler = AEApi.instance().registries().cell().getCellInventory(stack, null, StorageChannel.FLUIDS)
    if (!handler.isInstanceOf[IHandlerFluidStorage])
      return stack

    val cellHandler = handler.asInstanceOf[IHandlerFluidStorage]
    if (cellHandler.usedBytes() > 0)
      return stack

    val casing = ItemEnum.STORAGECASING.getDamagedStack(1)
    if (!player.inventory.addItemStackToInventory(casing))
      world.spawnEntityInWorld(new EntityItem(player.worldObj, player.posX, player.posY, player.posZ, casing))
    ItemEnum.STORAGECOMPONENT.getDamagedStack(stack.getMetadata + 4)
  }

  override def getRarity(stack: ItemStack) : EnumRarity = EnumRarity.rare
  override def getMaxTypes(stack: ItemStack) : Int = 5
  override def getMaxBytes(stack: ItemStack) : Int = spaces(Math.max(0,stack.getMetadata))
  override def getBytesPerType(stack: ItemStack): Int = getMaxBytes(stack) / 128
  override def getUpgradesInventory(stack: ItemStack) : IInventory = new InventoryECUpgrades("upgradesFluidCell", 2,
    stack, Some(Set(AEApi.instance.definitions.materials.cardFuzzy, AEApi.instance.definitions.materials.cardCapacity)))
  override def getConfigInventory(stack: ItemStack): IInventory = new InventoryECFluidConfig("configFluidCell", stack)
  override def getPreformatted(stack: ItemStack): util.ArrayList[Fluid] = {
    val preformatList = new util.ArrayList[Fluid]
    if (!stack.hasTagCompound)
      return preformatList
    val tagList = stack.getTagCompound.getTagList("ec:preformatConfig", Constants.NBT.TAG_STRING)
    for (i <- 0 until tagList.tagCount) {
      val fluid = FluidRegistry.getFluid(tagList.getStringTagAt(i))
      if (fluid != null)
        preformatList.add(fluid)
    }
    preformatList
  }

  //Client-sided stuff
  override def addInformation(stack: ItemStack, player: EntityPlayer, list: JavaList[_], bool: Boolean): Unit = {
    val strList: JavaList[String] = list.asInstanceOf[JavaList[String]]

    val handler = AEApi.instance().registries().cell().getCellInventory(stack, null, StorageChannel.FLUIDS)
    if (!handler.isInstanceOf[IHandlerFluidStorage])
      return

    val cellHandler = handler.asInstanceOf[IHandlerFluidStorage]
    val usedBytes: Long = cellHandler.usedBytes()

    strList.add(String.format(StatCollector.translateToLocal("extracells.tooltip.storage.fluid.bytes"), usedBytes / 250, cellHandler.totalBytes() / 250))
    strList.add(String.format(StatCollector.translateToLocal("extracells.tooltip.storage.fluid.types"), cellHandler.usedTypes, cellHandler.totalTypes))
    if (usedBytes != 0)
      strList.add(String.format(StatCollector.translateToLocal("extracells.tooltip.storage.fluid.content"), usedBytes))
    if (cellHandler.isFormatted)
      strList.add(StatCollector.translateToLocal("gui.appliedenergistics2.Partitioned") + " - " + StatCollector.translateToLocal("gui.appliedenergistics2.Precise"))
  }
  override def registerIcons(iconRegister: IIconRegister): Unit = {
    for (i: Int <- suffixes.indices)
      this.icons(i) = iconRegister.registerIcon("extracells:" + "storage.fluid" + suffixes(i))
  }

  override def getUnlocalizedName(itemStack: ItemStack) : String = "extracells.item.storage.fluid." +
    suffixes(itemStack.getMetadata)
  override def getIconFromDamage(dmg: Int) : IIcon = this.icons(MathHelper.clamp_int(dmg, 0, suffixes.length))
  override def getSubItems(item: Item, tab: CreativeTabs, list: JavaList[_]): Unit = {
    //Workaround for scala hating untyped generics
    for (i : Int <- suffixes.indices)
      list.asInstanceOf[JavaList[ItemStack]].add(new ItemStack(item, 1, i))
  }
}
