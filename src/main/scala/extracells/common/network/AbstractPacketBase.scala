package extracells.common.network

import cpw.mods.fml.common.network.simpleimpl.IMessage
import io.netty.buffer.ByteBuf

abstract class AbstractPacketBase extends IMessage{
  protected var mode: Byte = _

  def getMode: Byte = mode

  override def fromBytes(in: ByteBuf): Unit = {
    this.mode = in.readByte()
    readData(in)
  }

  override def toBytes(out: ByteBuf): Unit ={
    out.writeByte(this.mode)
    writeData(out)
  }

  def readData(in: ByteBuf): Unit
  def writeData(out: ByteBuf): Unit
}
