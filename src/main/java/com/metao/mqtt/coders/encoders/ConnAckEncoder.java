package com.metao.mqtt.coders.encoders;

import com.metao.mqtt.models.ConnAckPacket;
import com.metao.mqtt.models.PacketTypeMessage;
import com.metao.mqtt.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Mehrdad
 */
class ConnAckEncoder extends Encoder<ConnAckPacket> {

    @Override
    protected void encode(ChannelHandlerContext chc, ConnAckPacket message, ByteBuf out) {
        out.writeByte(PacketTypeMessage.CONNACK << 4);
        out.writeBytes(Utils.encodeRemainingLength(2));
        out.writeByte(message.isSessionPresent() ? 0x01 : 0x00);
        out.writeByte(message.getReturnCode());
    }

}
