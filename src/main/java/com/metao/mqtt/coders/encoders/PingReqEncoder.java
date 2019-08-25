package com.metao.mqtt.coders.encoders;

import com.metao.mqtt.models.PacketTypeMessage;
import com.metao.mqtt.models.PingRequestPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class PingReqEncoder implements Encoder<PingRequestPacket> {

    @Override
    public void encode(ChannelHandlerContext channelHandlerContext, PingRequestPacket msg, ByteBuf byteBuf) {
        byteBuf.writeByte(PacketTypeMessage.PINGREQ << 4).writeByte(0);
    }
}
