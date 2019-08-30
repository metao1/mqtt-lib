package com.metao.mqtt.coders.encoders;

import com.metao.mqtt.models.DisconnectPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Mehrdad
 */
public class DisconnectEncoder extends Encoder<DisconnectPacket> {

    @Override
    protected void encode(ChannelHandlerContext chc, DisconnectPacket msg, ByteBuf out) {
        out.writeByte(DisconnectPacket.DISCONNECT << 4).writeByte(0);
    }

}
