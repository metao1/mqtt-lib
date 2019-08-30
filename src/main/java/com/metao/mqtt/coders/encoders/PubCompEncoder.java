package com.metao.mqtt.coders.encoders;

import com.metao.mqtt.models.PacketTypeMessage;
import com.metao.mqtt.models.PubCompPacket;
import com.metao.mqtt.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Mehrdad
 */
class PubCompEncoder extends Encoder<PubCompPacket> {

    @Override
    protected void encode(ChannelHandlerContext chc, PubCompPacket msg, ByteBuf out) {
        out.writeByte(PacketTypeMessage.PUBCOMP << 4);
        out.writeBytes(Utils.encodeRemainingLength(2));
        out.writeShort(msg.getPacketId());
    }
}
