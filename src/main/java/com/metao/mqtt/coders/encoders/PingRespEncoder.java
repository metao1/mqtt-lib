package com.metao.mqtt.coders.encoders;

import com.metao.mqtt.models.PacketTypeMessage;
import com.metao.mqtt.models.PingResponsePacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class PingRespEncoder implements Encoder<PingResponsePacket> {
    @Override
    public void encode(ChannelHandlerContext channelHandlerContext, PingResponsePacket msg, ByteBuf byteBuf) {
        byteBuf.writeByte(PacketTypeMessage.PINGRESP << 4).writeByte(0);
    }
}
