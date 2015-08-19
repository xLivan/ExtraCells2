package extracells.common.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.relauncher.Side;
import extracells.common.network.handler.HandlerFluidStorage;
import extracells.common.network.packet.PacketFluidStorage;

public enum MessageEnum {
    PacketFluidStorageServer(HandlerFluidStorage.class, PacketFluidStorage.class, Side.SERVER),
    PacketFluidStorageClient(HandlerFluidStorage.class, PacketFluidStorage.class, Side.CLIENT);

    final Class<? extends IMessageHandler> handlerClass;
    final Class<? extends IMessage> messageClass;
    final Side side;

    <REQ extends IMessage, REPLY extends IMessage>
    MessageEnum(Class<? extends IMessageHandler<REQ, REPLY>> handlerClass,
                Class<? extends REQ> messageClass, Side side) {
        this.handlerClass = handlerClass;
        this.messageClass = messageClass;
        this.side = side;
    }
}
