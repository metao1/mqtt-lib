package com.metao.mqtt.coders.encoders;

import com.metao.mqtt.models.PacketTypeMessage;
import com.metao.mqtt.models.PublishPacket;
import com.metao.mqtt.models.QosType;
import com.metao.mqtt.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

class PublishEncoder implements Encoder<PublishPacket> {

    @Override
    public void encode(ChannelHandlerContext ctx, PublishPacket message, ByteBuf out) {
        if (message.getQos() == QosType.RESERVED) {
            throw new IllegalArgumentException("Found a message with RESERVED Qos");
        }
        if (message.getTopicName() == null || message.getTopicName().isEmpty()) {
            throw new IllegalArgumentException("Found a message with empty or null topic name");
        }

        ByteBuf variableHeaderBuff = ctx.alloc().buffer(2);
        ByteBuf buff = null;
        try {
            variableHeaderBuff.writeBytes(Utils.encodeStringToByteBuff(message.getTopicName()));
            if (message.getQos() == QosType.LEAST_ONE ||
                message.getQos() == QosType.EXACTLY_ONCE) {
                if (message.getPacketId() == null) {
                    throw new IllegalArgumentException("Found a message with QOS 1 or 2 and not MessageID setted");
                }
                variableHeaderBuff.writeShort(message.getPacketId());
            }
            variableHeaderBuff.writeBytes(message.getPayload());
            int variableHeaderSize = variableHeaderBuff.readableBytes();

            byte flags = Utils.encodeFlags(message);

            buff = ctx.alloc().buffer(2 + variableHeaderSize);
            buff.writeByte(PacketTypeMessage.PUBLISH << 4 | flags);
            buff.writeBytes(Utils.encodeRemainingLength(variableHeaderSize));
            buff.writeBytes(variableHeaderBuff);
            out.writeBytes(buff);
        } finally {
            variableHeaderBuff.release();
            if (buff != null) {
                buff.release();
            }
        }
    }

}
