package extracells.core.common.network

import cpw.mods.fml.common.network.NetworkRegistry
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper
import cpw.mods.fml.relauncher.Side
import extracells.core.common.network.handler.HandlerFluidStorage
import extracells.core.common.network.packet.PacketFluidStorage
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.network.Packet
import net.minecraft.world.World

object NetworkWrapper {
  private val CHANNEL: SimpleNetworkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel("extracells")
  private var currentDiscriminator: Int = 0
  val packets = Set.apply(
    (classOf[HandlerFluidStorage], classOf[PacketFluidStorage], Side.SERVER),
    (classOf[HandlerFluidStorage], classOf[PacketFluidStorage], Side.CLIENT)
  )

  /**
   * Registers all packets listed in [[packets]](
   */
  def registerMessages(): Unit = {
    this.packets.foreach(p => {
      CHANNEL.registerMessage(p._1, p._2, currentDiscriminator, p._3)
      currentDiscriminator += 1
    })
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