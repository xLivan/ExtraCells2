package extracells.common.block

import scala.annotation.switch

import extracells.ExtraCells
import extracells.common.tile.TileWalrus
import net.minecraft.block.material.Material
import net.minecraft.block.{ITileEntityProvider, Block}
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.MathHelper
import net.minecraft.world.{IBlockAccess, World}
import net.minecraftforge.common.util.ForgeDirection

class BlockWalrus extends Block(Material.clay) with ITileEntityProvider  {
  setCreativeTab(ExtraCells.ModTab)
  setHardness(2.0F)
  setResistance(10.0F)

  override def createNewTileEntity(world: World, meta: Int): TileEntity = new TileWalrus
  override def getRenderType: Int = -1
  override def getUnlocalizedName = super.getUnlocalizedName.replace("tile.","")
  override def isOpaqueCube = false
  override def renderAsNormalBlock = false

  override def onBlockPlacedBy(world: World, x: Int, y: Int, z: Int,
                               player: EntityLivingBase, itemStack: ItemStack) {
    val l = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3

    if (l == 0)
      world.setBlockMetadataWithNotify(x,y,z,2,2)
    if (l == 1)
      world.setBlockMetadataWithNotify(x,y,z,5,2)
    if (l == 2)
      world.setBlockMetadataWithNotify(x,y,z,3,2)
    if(l == 3)
      world.setBlockMetadataWithNotify(x,y,z,4,2)
  }

  override def setBlockBoundsBasedOnState(blockAccess: IBlockAccess, x: Int, y: Int, z: Int) {
    val direction: ForgeDirection = ForgeDirection.getOrientation(blockAccess.getBlockMetadata(x,y,z))
    direction match {
      case ForgeDirection.NORTH => setBlockBounds(0.0F, 0.0F, -1.0F, 1.0F, 1.0F, 1.0F)
      case ForgeDirection.EAST => setBlockBounds(0.0F, 0.0F, 0.0F, 2.0F, 1.0F, 1.0F)
      case ForgeDirection.SOUTH => setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 2.0F)
      case ForgeDirection.WEST => setBlockBounds(-1.0F, 0.0F,0.0F, 1.0F, 1.0F, 1.0F)
      case whoa => setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F)
    }
  }
}
