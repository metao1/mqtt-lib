package com.metao.mqtt.coders.encoders;

import com.metao.mqtt.models.PacketTypeMessage;
import com.metao.mqtt.models.PingRequestPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Mehrdad
 */
class PingReqEncoder extends Encoder<PingRequestPacket> {

    @Override
    protected void encode(ChannelHandlerContext chc, PingRequestPacket msg, ByteBuf out) {
        out.writeByte(PacketTypeMessage.PINGREQ << 4).writeByte(0);
    }
}
