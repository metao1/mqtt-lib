package de.vispiron.carsync.mqtt.coders;

import de.vispiron.carsync.mqtt.models.PacketTypeMessage;
import de.vispiron.carsync.mqtt.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Mehrdad A.Karami at 2/28/19
 **/

public class ConnectEncoder implements Encoder {

	@Override
	public void encode(ChannelHandlerContext channelHandlerContext, PacketTypeMessage msg, ByteBuf byteBuf) {
		ByteBuf headerBuffer = channelHandlerContext.alloc().buffer(12);//create header
		ByteBuf payloadBuffer = channelHandlerContext.alloc().buffer();//create an empty payload
		ByteBuf variableHeaderBuffer = channelHandlerContext.alloc().buffer(12);
		try {
			headerBuffer.writeBytes(Utils.encodeStringToByteBuff("MQIsdp"));
		} finally {
			//release the resources
			headerBuffer.release();
			payloadBuffer.release();
			variableHeaderBuffer.release();
		}
	}
}
