package com.metao.mqtt.coders.decoders;

import com.metao.mqtt.models.PacketTypeMessage;
import com.metao.mqtt.models.PublishDecoder;
import com.metao.mqtt.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mehrdad A.Karami at 2/26/19
 **/

public class MQTTDecoder extends ByteToMessageDecoder {

	private final Map<Byte, Decoder> decoderMap = new HashMap<>();

	/**
	 * Giving different packets type different header analysis
	 */
	public MQTTDecoder() {
        decoderMap.put(PacketTypeMessage.CONNECT, new ConnectDecoder());
        decoderMap.put(PacketTypeMessage.CONNACK, new ConnAckDecoder());
        decoderMap.put(PacketTypeMessage.PUBLISH, new PublishDecoder());
        decoderMap.put(PacketTypeMessage.PUBACK, new PublishAckDecoder());
        decoderMap.put(PacketTypeMessage.SUBSCRIBE, new SubscribeDecoder());
        decoderMap.put(PacketTypeMessage.SUBACK, new SubscribeAckDecoder());
        decoderMap.put(PacketTypeMessage.UNSUBSCRIBE, new UnsubscribeDecoder());
        decoderMap.put(PacketTypeMessage.DISCONNECT, new DisconnectDecoder());
        decoderMap.put(PacketTypeMessage.PINGREQ, new PingReqDecoder());
        decoderMap.put(PacketTypeMessage.PINGRESP, new PingRespDecoder());
        decoderMap.put(PacketTypeMessage.UNSUBACK, new UnsubAckDecoder());
        decoderMap.put(PacketTypeMessage.PUBCOMP, new PubCompDecoder());
        decoderMap.put(PacketTypeMessage.PUBREC, new PubRecDecoder());
        decoderMap.put(PacketTypeMessage.PUBREL, new PubRelDecoder());
	}

	/**
	 * First time the packets comes here for the decoding
	 *
	 * @param ctx the channel where the bytes come
	 * @param in  input buffer
	 * @param out output decoded objects as reference
	 * @throws Exception if some misunderstanding
	 */
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		in.markReaderIndex();//Reposition of the reader flag
		if (!Utils.checkValidHeader(in)) {
			in.resetReaderIndex();//return back to the first position in the buffer
			return;
		}
		in.resetReaderIndex();
		byte messageType = Utils.readMessageType(in);
		Decoder decoder = decoderMap.get(messageType);//after knowing what message type it is, we need to decode it
		if (decoder == null) {
			throw new CorruptedFrameException("Can't find any suitable decoder for message type " + messageType);
		}
		decoder.decode(ctx, in, out);
	}
}
