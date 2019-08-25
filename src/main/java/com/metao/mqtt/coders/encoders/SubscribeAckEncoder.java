package com.metao.mqtt.coders.encoders;

import com.metao.mqtt.models.PacketTypeMessage;
import com.metao.mqtt.coders.encoders.Encoder;
import com.metao.mqtt.models.QosType;
import com.metao.mqtt.models.SubscribeAckMessage;
import com.metao.mqtt.models.SubscribeAckPacket;
import com.metao.mqtt.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import javax.rmi.CORBA.Util;

/**
 * @author Mehrdad A.Karami at 3/7/19
 **/

public class SubscribeAckEncoder implements Encoder<SubscribeAckPacket> {

	@Override
	public void encode(ChannelHandlerContext channelHandlerContext, SubscribeAckPacket message, ByteBuf byteBuf) {
        if (message.types().isEmpty()) {
            throw new IllegalArgumentException("Found a suback message with empty topics");
        }

        int variableHeaderSize = 2 + message.types().size();
        ByteBuf buff = channelHandlerContext.alloc().buffer(6 + variableHeaderSize);
        try {
            buff.writeByte(PacketTypeMessage.SUBACK << 4);
            buff.writeBytes(Utils.encodeRemainingLength(variableHeaderSize));
            buff.writeShort(message.getPacketId());
            for (QosType c : message.types()) {
                buff.writeByte(c.byteValue());
            }

            byteBuf.writeBytes(buff);
        } finally {
            buff.release();
        }
	}
}
