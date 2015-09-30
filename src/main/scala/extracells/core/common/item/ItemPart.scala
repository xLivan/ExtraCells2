package extracells.core.common.item

import java.util
import java.util.function.BiConsumer

import appeng.api.AEApi
import appeng.api.config.Upgrades
import appeng.api.implementations.items.IItemGroup
import appeng.api.parts.{IPart, IPartItem}
import cpw.mods.fml.common.FMLLog
import cpw.mods.fml.relauncher.{Side, SideOnly}
import extracells.core.common.registries.PartEnum
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{EnumRarity, Item, ItemStack}
import net.minecraft.util.MathHelper
import net.minecraft.world.World
import org.apache.logging.log4j.Level

object ItemPart extends Item with IPartItem with IItemGroup {
  setMaxDurability(0)
  setHasSubtypes(true)
  AEApi.instance().partHelper().setItemBusRenderer(this)

  val values: Seq[PartEnum] = PartEnum.values()
  values.foreach(p => p.getUpgrades.forEach(new BiConsumer[Upgrades, Integer] {
    override def accept(u: Upgrades, i: Integer): Unit = {
      u.registerItem(new ItemStack(ItemPart.this, 1, p.ordinal()), i)
    }
  }))

  override def createPartFromItemStack(stack: ItemStack): IPart = {
    try {
      PartEnum.values()(MathHelper
        .clamp_int(stack.getMetadata, 0, PartEnum.values().length - 1))
        .newInstance(stack)
    }
    catch {
      case ex: Throwable => FMLLog.log(Level.ERROR, ex,
          "ExtraCells2 severe error - could not create AE2 Part from ItemStack! This should not happen!\n"
            + "[ExtraCells2 SEVERE] Please report this error on the GitHub repository\n"
            + "[ExtraCells2 SEVERE] Offending item: '%s'",
          stack.toString
        )
        null
    }
  }

  override def onItemUse(stack: ItemStack, player: EntityPlayer, world: World,
                          x: Int, y: Int, z: Int, side: Int,
                          hitX: Float, hitY: Float, hitZ: Float) = {
    AEApi.instance().partHelper().placeBus(stack, x, y, z, side, player, world)
  }

  override def registerIcons(iconReg: IIconRegister): Unit = {}

  override def getItemStackDisplayName(stack: ItemStack): String = {
    val stackOpt = Option(stack)
    //TODO: Interface name
    stackOpt.map(s => super.getItemStackDisplayName(s)).orNull
  }

  override def getRarity(stack: ItemStack): EnumRarity = {
    val stackOpt = Option(stack)
    stackOpt.filter {
      case _ => false
    }.fold(EnumRarity.rare)(s => super.getRarity(s))
  }

  override def getSubItems(item: Item, creativeTab: CreativeTabs,
                           list: util.List[_]): Unit = {
    for (i <- 0 until PartEnum.values().length)
      list.asInstanceOf[util.List[ItemStack]]
        .add(new ItemStack(item, 1, i))
  }

  override def getUnlocalizedGroupName(otherItems: util.Set[ItemStack],
                                       stack: ItemStack): String = {
    PartEnum.values()(MathHelper
      .clamp_int(stack.getMetadata, 0, PartEnum.values().length) - 1)
      .getGroupName
  }

  override def getUnlocalizedName(stack: ItemStack): String = {
    PartEnum.values()(MathHelper
      .clamp_int(stack.getMetadata, 0, PartEnum.values().length - 1))
      .getUnlocalizedName
  }

  @SideOnly(Side.CLIENT)
  override def getSpriteNumber = 0
}
