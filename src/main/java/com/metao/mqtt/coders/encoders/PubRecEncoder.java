package com.metao.mqtt.coders.encoders;

import com.metao.mqtt.models.PacketTypeMessage;
import com.metao.mqtt.models.PubRecPacket;
import com.metao.mqtt.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class PubRecEncoder implements Encoder<PubRecPacket> {
    @Override
    public void encode(ChannelHandlerContext channelHandlerContext, PubRecPacket msg, ByteBuf byteBuf) {
        byteBuf.writeByte(PacketTypeMessage.PUBREC << 4);
        byteBuf.writeBytes(Utils.encodeRemainingLength(2));
        byteBuf.writeShort(msg.getPacketId());
    }
}
