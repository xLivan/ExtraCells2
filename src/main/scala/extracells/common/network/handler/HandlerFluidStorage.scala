package extracells.common.network.handler

import cpw.mods.fml.common.network.simpleimpl.{IMessage, IMessageHandler, MessageContext}
import cpw.mods.fml.relauncher.Side
import extracells.client.gui.GuiFluidStorage
import extracells.common.container.implementations.ContainerFluidStorage
import extracells.common.network.packet.PacketFluidStorage
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer

class HandlerFluidStorage extends IMessageHandler[PacketFluidStorage, IMessage] {
  override def onMessage(message: PacketFluidStorage, ctx: MessageContext): Unit = {
    val player: EntityPlayer = ctx.side match  {
      case Side.SERVER => ctx.getServerHandler.playerEntity
      case Side.CLIENT => Minecraft.getMinecraft.thePlayer
    }

    //Side checking so packet modes only go one way.
    (ctx.side, message.getMode) match {
      case (Side.CLIENT, 0) => return
      case (Side.SERVER, 2) => return
      case (Side.SERVER, 3) => return
    }

    message.getMode match {
      case 0 => player.openContainer match {
        case container: ContainerFluidStorage =>
          container.forceFluidUpdate()
          container.doWork()
      }
      case 1 => player.openContainer match {
        case container: ContainerFluidStorage => container
          .receiveSelectedFluid(message.getCurrentFluid)
      }
      case 2 => Minecraft.getMinecraft.currentScreen match {
        case storage: GuiFluidStorage => storage
          .inventorySlots.asInstanceOf[ContainerFluidStorage]
          .hasWirelessTermHandler = message.hasTermHandler
      }
      case 3 => Minecraft.getMinecraft.currentScreen match {
        case storage: GuiFluidStorage => storage
          .inventorySlots.asInstanceOf[ContainerFluidStorage]
          .updateFluidList(message.getFluidList)
      }
    }
  }
}
