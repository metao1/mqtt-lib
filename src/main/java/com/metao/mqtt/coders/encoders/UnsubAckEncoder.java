package com.metao.mqtt.coders.encoders;

import com.metao.mqtt.models.UnsubscribeAckPacket;
import com.metao.mqtt.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Mehrdad
 */
class UnsubAckEncoder extends Encoder<UnsubscribeAckPacket> {

    @Override
    protected void encode(ChannelHandlerContext chc, UnsubscribeAckPacket msg, ByteBuf out) {
        out.writeByte(UnsubscribeAckPacket.UNSUBACK << 4).
            writeBytes(Utils.encodeRemainingLength(2)).
            writeShort(msg.getPacketId());
    }
}