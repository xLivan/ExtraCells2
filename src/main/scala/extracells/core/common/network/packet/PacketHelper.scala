package extracells.core.common.network.packet

import java.util.UUID

import appeng.api.parts.IPartHost
import com.google.common.base.Charsets
import cpw.mods.fml.common.FMLCommonHandler
import cpw.mods.fml.relauncher.Side
import extracells.core.common.part.PartECBase
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.{World, WorldServer}
import net.minecraftforge.common.DimensionManager
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.{Fluid, FluidRegistry}

object PacketHelper {

  //Read helpers
  def readString(in: ByteBuf): String = {
    val stringBytes: Array[Byte] = new Array[Byte](in.readInt())
    in.readBytes(stringBytes)
    new String(stringBytes, Charsets.UTF_8)
  }

  def readWorld(in: ByteBuf): World = {
    val world: WorldServer = DimensionManager.getWorld(in.readInt())
    if (FMLCommonHandler.instance.getSide.equals(Side.CLIENT))
      return if (world != null) world else Minecraft.getMinecraft.theWorld
    world
  }

  def readPlayer(in: ByteBuf): EntityPlayer = {
    if(!in.readBoolean())
      return null
    readWorld(in).getPlayerEntityByUUID(UUID.fromString(readString(in)))
  }

  def readTileEntity(in: ByteBuf): TileEntity =
    readWorld(in).getTileEntity(in.readInt(), in.readInt(), in.readInt())

  def readPart(in: ByteBuf): PartECBase = {
    readTileEntity(in).asInstanceOf[IPartHost]
      .getPart(ForgeDirection.getOrientation(in.readByte())).asInstanceOf[PartECBase]
  }

  def readFluid(in: ByteBuf): Fluid = FluidRegistry.getFluid(readString(in))

  //Write helpers
  def writeString(str: String, out: ByteBuf): Unit = {
    val strBytes: Array[Byte] = str.getBytes(Charsets.UTF_8)
    out.writeInt(strBytes.length)
    out.writeBytes(strBytes)
  }

  def writeWorld(world: World, out: ByteBuf): Unit =
    out.writeInt(world.provider.dimensionId)

  def writePlayer(player: EntityPlayer, out: ByteBuf): Unit = {
    if (player == null) {
      out.writeBoolean(false)
      return
    }
    out.writeBoolean(true)
    writeWorld(player.worldObj, out)
    writeString(player.getUniqueID.toString, out)
  }

  def writeTileEntity(tile: TileEntity, out: ByteBuf): Unit = {
    writeWorld(tile.getWorld, out)
    out.writeInt(tile.xCoord)
    out.writeInt(tile.yCoord)
    out.writeInt(tile.zCoord)
  }

  def writePart(part: PartECBase, out: ByteBuf): Unit = {
    part.getHost.foreach(ph => writeTileEntity(ph.getTile, out))
    out.writeByte(part.getSide.ordinal)
  }

  def writeFluid(fluid: Fluid, out: ByteBuf): Unit = {
    if (fluid == null)
      writeString("", out)
    else
      writeString(fluid.getName, out)
  }
}
