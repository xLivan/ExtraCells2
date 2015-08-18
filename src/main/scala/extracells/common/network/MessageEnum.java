package extracells.common.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.relauncher.Side;

public enum MessageEnum {
    Test(IMessageHandler.class, IMessage.class, 99, Side.CLIENT)
    ;

    final Class<? extends IMessageHandler> handlerClass;
    final Class<? extends IMessage> messageClass;
    final int discriminator;
    final Side side;

    <REQ extends IMessage, REPLY extends IMessage>
    MessageEnum(Class<? extends IMessageHandler<REQ, REPLY>> handlerClass,
                Class<? extends REQ> messageClass, int discriminator, Side side) {
        this.handlerClass = handlerClass;
        this.messageClass = messageClass;
        this.discriminator = discriminator;
        this.side = side;
    }
}
