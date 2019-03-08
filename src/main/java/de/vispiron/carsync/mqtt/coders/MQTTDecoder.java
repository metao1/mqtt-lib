package de.vispiron.carsync.mqtt.coders;

import de.vispiron.carsync.mqtt.models.PacketTypeMessage;
import de.vispiron.carsync.mqtt.utils.Utils;
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
		decoderMap.put(PacketTypeMessage.SUBSCRIBE, new SubscribeDecoder());
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
