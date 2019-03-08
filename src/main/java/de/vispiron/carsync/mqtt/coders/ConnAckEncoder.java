package de.vispiron.carsync.mqtt.coders;

import de.vispiron.carsync.mqtt.models.ConnAckPacket;
import de.vispiron.carsync.mqtt.models.PacketTypeMessage;
import de.vispiron.carsync.mqtt.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CorruptedFrameException;

/**
 * @author Mehrdad A.Karami at 2/27/19
 **/

public class ConnAckEncoder implements Encoder<ConnAckPacket> {

	@Override
	public void encode(ChannelHandlerContext channelHandlerContext, ConnAckPacket msg, ByteBuf byteBuf)
			throws CorruptedFrameException {
		byteBuf.writeByte(PacketTypeMessage.CONNACK << 4);
		byteBuf.writeBytes(Utils.encodeRemainingLength(2));
		byteBuf.writeByte(msg.isSessionPresent() ? 0x01 : 0x00);
		byteBuf.writeByte(msg.getReturnCode());
	}
}
