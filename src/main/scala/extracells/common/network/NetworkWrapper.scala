package extracells.common.network

import cpw.mods.fml.common.network.NetworkRegistry
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.network.Packet
import net.minecraft.world.World

object NetworkWrapper {
  private val CHANNEL: SimpleNetworkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel("extracells")

  def registerMessages(): Unit = {
    for ( messageType <- MessageEnum.values) {
      CHANNEL.registerMessage(messageType.handlerClass,
        messageType.messageClass, messageType.discriminator, messageType.side)
    }
  }

  def sendToServer(message: PacketBase) : Unit =
    CHANNEL.sendToServer(message)

  def sendToAll(message: PacketBase): Unit =
    CHANNEL.sendToAll(message)

  def sendToDimension(message: PacketBase, dimId: Int): Unit =
    CHANNEL.sendToDimension(message, dimId)

  def sendToPlayersAround(message: PacketBase, point: TargetPoint): Unit =
    CHANNEL.sendToAllAround(message, point)

  def sendToPlayer(message: PacketBase, player: EntityPlayerMP) : Unit =
    CHANNEL.sendTo(message, player)

  def sendPacketToWorld(packet: Packet, world: World): Unit = {
    for (player: Object <- world.playerEntities if (player.isInstanceOf[EntityPlayerMP]))
      player.asInstanceOf[EntityPlayerMP].playerNetServerHandler.sendPacket(packet)
  }
}