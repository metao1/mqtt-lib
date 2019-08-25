package com.metao.mqtt.coders.encoders;

import com.metao.mqtt.models.PacketIdMessage;
import com.metao.mqtt.models.PubCompPacket;
import com.metao.mqtt.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class PubCompEncoder implements Encoder<PubCompPacket> {

    @Override
    public void encode(ChannelHandlerContext channelHandlerContext, PubCompPacket msg, ByteBuf byteBuf) {
        byteBuf.writeByte(PacketIdMessage.PUBCOMP << 4);
        byteBuf.writeBytes(Utils.encodeRemainingLength(2));
        byteBuf.writeShort(msg.getPacketId());
    }
}
