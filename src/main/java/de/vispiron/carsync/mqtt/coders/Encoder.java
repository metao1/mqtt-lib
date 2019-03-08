package de.vispiron.carsync.mqtt.coders;

import de.vispiron.carsync.mqtt.models.PacketTypeMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Mehrdad A.Karami at 2/28/19
 **/

public interface Encoder<T extends PacketTypeMessage> {

	void encode(ChannelHandlerContext channelHandlerContext, T msg, ByteBuf byteBuf);
}
