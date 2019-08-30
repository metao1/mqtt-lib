package com.metao.mqtt.coders.encoders;

import com.metao.mqtt.models.PacketTypeMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mehrdad
 */
public class MQTTEncoder extends MessageToByteEncoder<PacketTypeMessage> {

    private Map<Byte, Encoder> m_encoderMap = new HashMap<Byte, Encoder>();

    public MQTTEncoder() {
        m_encoderMap.put(PacketTypeMessage.CONNECT, new ConnectEncoder());
        m_encoderMap.put(PacketTypeMessage.CONNACK, new ConnAckEncoder());
        m_encoderMap.put(PacketTypeMessage.PUBLISH, new PublishEncoder());
        m_encoderMap.put(PacketTypeMessage.PUBACK, new PubAckEncoder());
        m_encoderMap.put(PacketTypeMessage.SUBSCRIBE, new SubscribeEncoder());
        m_encoderMap.put(PacketTypeMessage.SUBACK, new SubAckEncoder());
        m_encoderMap.put(PacketTypeMessage.UNSUBSCRIBE, new UnsubscribeEncoder());
        m_encoderMap.put(PacketTypeMessage.DISCONNECT, new DisconnectEncoder());
        m_encoderMap.put(PacketTypeMessage.PINGREQ, new PingReqEncoder());
        m_encoderMap.put(PacketTypeMessage.PINGRESP, new PingRespEncoder());
        m_encoderMap.put(PacketTypeMessage.UNSUBACK, new UnsubAckEncoder());
        m_encoderMap.put(PacketTypeMessage.PUBCOMP, new PubCompEncoder());
        m_encoderMap.put(PacketTypeMessage.PUBREC, new PubRecEncoder());
        m_encoderMap.put(PacketTypeMessage.PUBREL, new PubRelEncoder());
    }

    @Override
    protected void encode(ChannelHandlerContext chc, PacketTypeMessage msg, ByteBuf bb) throws Exception {
        Encoder encoder = m_encoderMap.get(msg.getMessageType());
        if (encoder == null) {
            throw new CorruptedFrameException("Can't find any suitable decoder for message type: " + msg.getMessageType());
        }
        encoder.encode(chc, msg, bb);
    }
}
