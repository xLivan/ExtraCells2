package extracells.common.network

import cpw.mods.fml.common.network.simpleimpl.IMessage
import extracells.common.network.packet.PacketHelper
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer

abstract class AbstractPacketBase extends IMessage{
  protected var mode: Byte = _
  protected var player: EntityPlayer = _

  def this(player: EntityPlayer) {
    this()
    this.player = player
  }

  override def fromBytes(in: ByteBuf): Unit = {
    this.mode = in.readByte()
    this.player = PacketHelper.readPlayer(in)
    readData(in)
  }

  override def toBytes(out: ByteBuf): Unit ={
    out.writeByte(this.mode)
    PacketHelper.writePlayer(this.player, out)
    writeData(out)
  }

  def readData(in: ByteBuf): Unit
  def writeData(out: ByteBuf): Unit
  def execute(): Unit
}
