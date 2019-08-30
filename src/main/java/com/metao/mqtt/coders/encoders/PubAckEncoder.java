package com.metao.mqtt.coders.encoders;

import com.metao.mqtt.models.PacketTypeMessage;
import com.metao.mqtt.models.PublishAckPacket;
import com.metao.mqtt.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Mehrdad
 */
class PubAckEncoder extends Encoder<PublishAckPacket> {

    @Override
    protected void encode(ChannelHandlerContext chc, PublishAckPacket msg, ByteBuf out) {
        ByteBuf buff = chc.alloc().buffer(4);
        try {
            buff.writeByte(PacketTypeMessage.PUBACK << 4);
            buff.writeBytes(Utils.encodeRemainingLength(2));
            buff.writeShort(msg.getPacketId());
            out.writeBytes(buff);
        } finally {
            buff.release();
        }
    }

}
