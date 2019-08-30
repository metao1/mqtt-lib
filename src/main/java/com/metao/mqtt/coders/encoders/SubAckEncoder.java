package com.metao.mqtt.coders.encoders;

import com.metao.mqtt.models.QosType;
import com.metao.mqtt.models.SubscribeAckPacket;
import com.metao.mqtt.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Mehrdad
 */
class SubAckEncoder extends Encoder<SubscribeAckPacket> {

    @Override
    protected void encode(ChannelHandlerContext chc, SubscribeAckPacket message, ByteBuf out) {
        if (message.types().isEmpty()) {
            throw new IllegalArgumentException("Found a suback message with empty topics");
        }

        int variableHeaderSize = 2 + message.types().size();
        ByteBuf buff = chc.alloc().buffer(6 + variableHeaderSize);
        try {
            buff.writeByte(SubscribeAckPacket.SUBACK << 4);
            buff.writeBytes(Utils.encodeRemainingLength(variableHeaderSize));
            buff.writeShort(message.getPacketId());
            for (QosType c : message.types()) {
                buff.writeByte(c.byteValue());
            }

            out.writeBytes(buff);
        } finally {
            buff.release();
        }
    }

}
