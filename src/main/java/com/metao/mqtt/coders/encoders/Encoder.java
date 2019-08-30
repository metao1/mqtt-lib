package com.metao.mqtt.coders.encoders;

import com.metao.mqtt.models.PacketTypeMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Mehrdad
 */
abstract class Encoder<T extends PacketTypeMessage> {
    abstract protected void encode(ChannelHandlerContext chc, T msg, ByteBuf bb);
}
