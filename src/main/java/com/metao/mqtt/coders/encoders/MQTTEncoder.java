package com.metao.mqtt.coders.encoders;

import com.metao.mqtt.models.PacketTypeMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mehrdad A.Karami at 2/28/19
 **/

public class MQTTEncoder extends MessageToByteEncoder<PacketTypeMessage> {

	private final Map<Byte, Encoder> encoderMap = new HashMap<>();

	public MQTTEncoder() {
        encoderMap.put(PacketTypeMessage.CONNECT, new ConnectEncoder());
        encoderMap.put(PacketTypeMessage.CONNACK, new ConnAckEncoder());
        encoderMap.put(PacketTypeMessage.PUBLISH, new PublishEncoder());
        encoderMap.put(PacketTypeMessage.PUBACK, new PublishAckEncoder());
        encoderMap.put(PacketTypeMessage.SUBSCRIBE, new SubscribeEncoder());
        encoderMap.put(PacketTypeMessage.SUBACK, new SubscribeAckEncoder());
        encoderMap.put(PacketTypeMessage.UNSUBSCRIBE, new UnsubscribeEncoder());
        encoderMap.put(PacketTypeMessage.DISCONNECT, new DisconnectEncoder());
        encoderMap.put(PacketTypeMessage.PINGREQ, new PingReqEncoder());
        encoderMap.put(PacketTypeMessage.PINGRESP, new PingRespEncoder());
        encoderMap.put(PacketTypeMessage.UNSUBACK, new UnsubAckEncoder());
        encoderMap.put(PacketTypeMessage.PUBCOMP, new PubCompEncoder());
        encoderMap.put(PacketTypeMessage.PUBREC, new PubRecEncoder());
        encoderMap.put(PacketTypeMessage.PUBREL, new PubRelEncoder());
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, PacketTypeMessage msg, ByteBuf out) throws Exception {
		Encoder encoder = encoderMap.get(msg.getMessageType());
		if (encoder == null) {
			throw new CorruptedFrameException(
					"Can't find any suitable encoder for message type: " + msg.getMessageType());
		}
		encoder.encode(ctx, msg, out);
	}
}
