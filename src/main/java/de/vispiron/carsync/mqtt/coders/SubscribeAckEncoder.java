package de.vispiron.carsync.mqtt.coders;

import de.vispiron.carsync.mqtt.models.PacketTypeMessage;
import de.vispiron.carsync.mqtt.models.QosType;
import de.vispiron.carsync.mqtt.models.SubscribeAckMessage;
import de.vispiron.carsync.mqtt.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Mehrdad A.Karami at 3/7/19
 **/

public class SubscribeAckEncoder implements Encoder<SubscribeAckMessage> {

	@Override
	public void encode(ChannelHandlerContext channelHandlerContext, SubscribeAckMessage msg, ByteBuf byteBuf) {
		if (msg.getTypes().isEmpty()) {
			throw new IllegalArgumentException("Found a subscribe packet message with the empty topic!");
		}
		int variableHeaderSize = 2 + msg.getTypes().size();
		ByteBuf buffer = channelHandlerContext.alloc().buffer(6 + variableHeaderSize);
		try {
			if (buffer == null) {
				throw new NullPointerException("Buffer is null!");
			}
			buffer.writeByte(PacketTypeMessage.SUBSACK << 4);
			buffer.writeBytes(Utils.encodeRemainingLength(variableHeaderSize));
			for (QosType qosType : msg.getTypes()) {
				buffer.writeByte(qosType.byteValue());
			}
			byteBuf.writeBytes(buffer);
		} finally {
			if (buffer != null) {
				buffer.release();
			}
		}
	}
}
