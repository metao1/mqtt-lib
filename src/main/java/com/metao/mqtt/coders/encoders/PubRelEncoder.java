package com.metao.mqtt.coders.encoders;

import com.metao.mqtt.models.PubRelPacket;
import com.metao.mqtt.models.PacketTypeMessage;
import com.metao.mqtt.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class PubRelEncoder implements Encoder<PubRelPacket> {
    @Override
    public void encode(ChannelHandlerContext channelHandlerContext, PubRelPacket msg, ByteBuf byteBuf) {
        byteBuf.writeByte(PacketTypeMessage.PUBREL << 4 | 0x02);
        byteBuf.writeBytes(Utils.encodeRemainingLength(2));
        byteBuf.writeShort(msg.getPacketId());
    }
}
