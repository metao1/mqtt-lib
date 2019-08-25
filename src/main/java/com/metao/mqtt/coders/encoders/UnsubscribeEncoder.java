package com.metao.mqtt.coders.encoders;

import com.metao.mqtt.models.PacketTypeMessage;
import com.metao.mqtt.models.UnsubscribePacket;
import com.metao.mqtt.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class UnsubscribeEncoder implements Encoder<UnsubscribePacket> {

    @Override
    public void encode(ChannelHandlerContext channelHandlerContext, UnsubscribePacket msg, ByteBuf byteBuf) {
        byteBuf.writeByte(PacketTypeMessage.UNSUBACK << 4).
            writeBytes(Utils.encodeRemainingLength(2)).
            writeShort(msg.getPacketId());
    }
}
