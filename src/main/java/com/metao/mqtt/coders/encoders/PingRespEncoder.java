package com.metao.mqtt.coders.encoders;

import com.metao.mqtt.models.PingResponsePacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Mehrdad
 */
class PingRespEncoder extends Encoder<PingResponsePacket> {

    @Override
    protected void encode(ChannelHandlerContext chc, PingResponsePacket msg, ByteBuf out) {
        out.writeByte(PingResponsePacket.PINGRESP << 4).writeByte(0);
    }
}
