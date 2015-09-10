package extracells.common.network

import cpw.mods.fml.common.network.NetworkRegistry
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.network.Packet
import net.minecraft.world.World

object NetworkWrapper {
  private val CHANNEL: SimpleNetworkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel("extracells")
  private var currentDiscriminator: Int = 0

  /**
   * Registers all packets listed in [[MessageEnum]](
   */
  def registerMessages(): Unit = {
    for ( messageType <- MessageEnum.values) {
      CHANNEL.registerMessage(messageType.handlerClass,
        messageType.messageClass, currentDiscriminator, messageType.side)
      currentDiscriminator += 1
    }
  }

  /**
   * Sends a packet to server
   *
   * @param message
   */
  def sendToServer(message: AbstractPacketBase) : Unit =
    CHANNEL.sendToServer(message)

  /**
   * Sends a packet to all players on server
   *
   * @param message
   */
  def sendToAll(message: AbstractPacketBase): Unit =
    CHANNEL.sendToAll(message)

  /**
   * Sends a packet to all players in a dimension
   *
   * @param message
   * @param dimId
   */
  def sendToDimension(message: AbstractPacketBase, dimId: Int): Unit =
    CHANNEL.sendToDimension(message, dimId)

  /**
   * Send a packet to all players around a point
   *
   * @param message
   * @param point
   */
  def sendToPlayersAround(message: AbstractPacketBase, point: TargetPoint): Unit =
    CHANNEL.sendToAllAround(message, point)

  /**
   * Sends a packet to a player
   *
   * @param message
   * @param player
   */
  def sendToPlayer(message: AbstractPacketBase, player: EntityPlayer) : Unit =
    CHANNEL.sendTo(message, player.asInstanceOf[EntityPlayerMP])
}