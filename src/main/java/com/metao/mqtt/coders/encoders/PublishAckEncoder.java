package com.metao.mqtt.coders.encoders;

import com.metao.mqtt.models.PacketTypeMessage;
import com.metao.mqtt.models.PublishAckPacket;
import com.metao.mqtt.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class PublishAckEncoder implements Encoder<PublishAckPacket> {
    @Override
    public void encode(ChannelHandlerContext channelHandlerContext, PublishAckPacket msg, ByteBuf byteBuf) {
        byteBuf.writeByte(PacketTypeMessage.UNSUBACK << 4).
            writeBytes(Utils.encodeRemainingLength(2)).
            writeShort(msg.getPacketId());
    }
}
