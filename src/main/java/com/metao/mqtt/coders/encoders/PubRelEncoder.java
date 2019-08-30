package com.metao.mqtt.coders.encoders;

import com.metao.mqtt.models.PubRelPacket;
import com.metao.mqtt.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Mehrdad
 */
class PubRelEncoder extends Encoder<PubRelPacket> {

    @Override
    protected void encode(ChannelHandlerContext chc, PubRelPacket msg, ByteBuf out) {
        out.writeByte(PubRelPacket.PUBREL << 4 | 0x02);
        out.writeBytes(Utils.encodeRemainingLength(2));
        out.writeShort(msg.getPacketId());
    }
}