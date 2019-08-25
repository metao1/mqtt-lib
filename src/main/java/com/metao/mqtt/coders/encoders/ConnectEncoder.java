package com.metao.mqtt.coders.encoders;

import com.metao.mqtt.models.PacketTypeMessage;
import com.metao.mqtt.models.ConnectPacket;
import com.metao.mqtt.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Mehrdad A.Karami at 2/28/19
 **/

public class ConnectEncoder implements Encoder<ConnectPacket> {


    @Override
    public void encode(ChannelHandlerContext channelHandlerContext, ConnectPacket message, ByteBuf byteBuf) {
        ByteBuf staticHeaderBuff = channelHandlerContext.alloc().buffer(12);
        ByteBuf buff = channelHandlerContext.alloc().buffer();
        ByteBuf variableHeaderBuff = channelHandlerContext.alloc().buffer(12);
        try {
            staticHeaderBuff.writeBytes(Utils.encodeStringToByteBuff("MQIsdp"));

            //version
            staticHeaderBuff.writeByte(0x03);

            //connection flags and Strings
            byte connectionFlags = 0;
            if (message.isCleanSession()) {
                connectionFlags |= 0x02;
            }
            if (message.isWillFlag()) {
                connectionFlags |= 0x04;
            }
            connectionFlags |= ((message.getWillQos() & 0x03) << 3);
            if (message.isWillRetain()) {
                connectionFlags |= 0x020;
            }
            if (message.isPasswordFlag()) {
                connectionFlags |= 0x040;
            }
            if (message.isUserFlag()) {
                connectionFlags |= 0x080;
            }
            staticHeaderBuff.writeByte(connectionFlags);

            //Keep alive timer
            staticHeaderBuff.writeShort(message.getKeepAlive());

            //Variable part
            if (message.getClientId() != null) {
                variableHeaderBuff.writeBytes(Utils.encodeStringToByteBuff(message.getClientId()));
                if (message.isWillFlag()) {
                    variableHeaderBuff.writeBytes(Utils.encodeStringToByteBuff((message.getWillTopic())));
                    variableHeaderBuff.writeBytes(Utils.encodeFixedLengthContent(message.getWillMessage()));
                }
                if (message.isUserFlag() && message.getUsername() != null) {
                    variableHeaderBuff.writeBytes(Utils.encodeStringToByteBuff((message.getUsername())));
                    if (message.isPasswordFlag() && message.getPassword() != null) {
                        variableHeaderBuff.writeBytes(Utils.encodeFixedLengthContent(message.getPassword()));
                    }
                }
            }

            int variableHeaderSize = variableHeaderBuff.readableBytes();
            buff.writeByte(PacketTypeMessage.CONNECT << 4);
            buff.writeBytes(Utils.encodeRemainingLength(12 + variableHeaderSize));
            buff.writeBytes(staticHeaderBuff).writeBytes(variableHeaderBuff);

            byteBuf.writeBytes(buff);
        } finally {
            staticHeaderBuff.release();
            buff.release();
            variableHeaderBuff.release();
        }
    }
}
