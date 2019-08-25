package com.metao.mqtt.coders.encoders;

import com.metao.mqtt.models.DisconnectPacket;
import com.metao.mqtt.models.ZeroLengthPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class DisconnectEncoder implements Encoder<DisconnectPacket> {

    @Override
    public void encode(ChannelHandlerContext channelHandlerContext, DisconnectPacket msg, ByteBuf byteBuf) {
        byteBuf.writeByte(ZeroLengthPacket.DISCONNECT << 4).writeByte(0);
    }
}
