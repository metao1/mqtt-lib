package com.metao.mqtt.coders.encoders;

import com.metao.mqtt.models.QosType;
import com.metao.mqtt.models.UnsubscribePacket;
import com.metao.mqtt.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;


/**
 * @author Mehrdad
 */
class UnsubscribeEncoder extends Encoder<UnsubscribePacket> {

    @Override
    protected void encode(ChannelHandlerContext chc, UnsubscribePacket message, ByteBuf out) {
        if (message.topicFilters().isEmpty()) {
            throw new IllegalArgumentException("Found an unsubscribe message with empty topics");
        }

        if (message.getQos() != QosType.LEAST_ONE) {
            throw new IllegalArgumentException("Expected a message with QOS 1, found " + message.getQos());
        }

        ByteBuf variableHeaderBuff = chc.alloc().buffer(4);
        ByteBuf buff = null;
        try {
            variableHeaderBuff.writeShort(message.getPacketId());
            for (String topic : message.topicFilters()) {
                variableHeaderBuff.writeBytes(Utils.encodeStringToByteBuff(topic));
            }

            int variableHeaderSize = variableHeaderBuff.readableBytes();
            byte flags = Utils.encodeFlags(message);
            buff = chc.alloc().buffer(2 + variableHeaderSize);

            buff.writeByte(UnsubscribePacket.UNSUBSCRIBE << 4 | flags);
            buff.writeBytes(Utils.encodeRemainingLength(variableHeaderSize));
            buff.writeBytes(variableHeaderBuff);

            out.writeBytes(buff);
        } finally {
            variableHeaderBuff.release();
            buff.release();
        }
    }

}
