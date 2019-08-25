package com.metao.mqtt.coders.encoders;

import com.metao.mqtt.models.PacketTypeMessage;
import com.metao.mqtt.models.QosType;
import com.metao.mqtt.models.SubscribePacket;
import com.metao.mqtt.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Mehrdad A.Karami at 3/7/19
 **/

public class SubscribeEncoder implements Encoder<SubscribePacket> {

	@Override
	public void encode(ChannelHandlerContext channelHandlerContext, SubscribePacket msg, ByteBuf byteBuf) {
		if (msg.getSubscriptions().isEmpty()) {
			throw new IllegalArgumentException("Found a subscription message with empty topic");
		}
		if (msg.getQos() != QosType.LEAST_ONE) {
			throw new IllegalArgumentException("Expected a message with QoS 1, found " + msg.getQos());
		}
		ByteBuf variableHeaderBuffer = channelHandlerContext.alloc().buffer(4);
		ByteBuf buffer = null;
		try {
			variableHeaderBuffer.writeShort(msg.getPacketId());
			msg.getSubscriptions().forEach(deCouple -> {
				variableHeaderBuffer.writeBytes(Utils.encodeStringToByteBuff(deCouple.getTopicFilter()));
				variableHeaderBuffer.writeByte(deCouple.getQos());
			});
			int readableVariableBytes = variableHeaderBuffer.readableBytes();
			byte flags = Utils.encodeFlags(msg);
			buffer = channelHandlerContext.alloc().buffer(2 + readableVariableBytes);
			buffer.writeByte(PacketTypeMessage.SUBSCRIBE << 4 | flags);
			buffer.writeBytes(Utils.encodeRemainingLength(readableVariableBytes));
			buffer.writeBytes(variableHeaderBuffer);
			byteBuf.writeBytes(buffer);
		} finally {
			variableHeaderBuffer.release();
			if (buffer != null) {
				buffer.release();
			}
		}
	}
}
