package com.metao.mqtt.coders.encoders;

import com.metao.mqtt.models.PubRecPacket;
import com.metao.mqtt.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Mehrdad
 */
class PubRecEncoder extends Encoder<PubRecPacket> {

    @Override
    protected void encode(ChannelHandlerContext chc, PubRecPacket msg, ByteBuf out) {
        out.writeByte(PubRecPacket.PUBREC << 4);
        out.writeBytes(Utils.encodeRemainingLength(2));
        out.writeShort(msg.getPacketId());
    }
}