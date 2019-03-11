package de.vispiron.carsync.mqtt.models;

import de.vispiron.carsync.mqtt.coders.Decoder;
import de.vispiron.carsync.mqtt.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.util.AttributeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Mehrdad A.Karami at 3/11/19
 **/

public class PublishDecoder extends Decoder {
	Logger log = LoggerFactory.getLogger(PublishDecoder.class);

	@Override
	protected void decode(AttributeMap map, ByteBuf in, List<Object> out) throws Exception {
		log.info("decode invoked with buffer {}", in);
		in.resetReaderIndex();
		int startPosition = in.readerIndex();
		PublishPacket publishPacket = new PublishPacket();
		if (!decodeCommonHeader(publishPacket, in)) {
			log.info("decode the data {}", in);
			in.resetReaderIndex();
			return;
		}
		if (Utils.isMqttVersion3_1_1(map)) {
			//bad protocol , if QoS = 0 => Dup = 0
			if (publishPacket.getQos() == QosType.MOST_ONE && publishPacket.isDupFlag()) {
				throw new CorruptedFrameException("Received a PUBLISH with QoS=0 & DUP = 1 , MQTT 3.1.1 violation");
			}
			if (publishPacket.getQos() == QosType.RESEREVED) {
				throw new CorruptedFrameException(
						"Received a PUBLISH with QoS flags settled 10 b11, MQTT 3.1.1 violation");
			}
			int remainingLength = publishPacket.getPacketLength();

			//Topic name

			String topicName = Utils.decodeByteBuffToString(in);
			if (!Utils.isValid(topicName)) {
				in.resetReaderIndex();
				return;
			}
			//todo
			//[MQTT-3.3.2-2] The topic name in the Publish packet must not contain wildcard characters
			if (topicName.length() == 0) {
				throw new CorruptedFrameException("Received a PUBLISH with topic without any character");
			}
			publishPacket.setTopicName(topicName);
			if (publishPacket.getQos() == QosType.LEAST_ONE || publishPacket.getQos() == QosType.EXACTLY_ONCE) {
				publishPacket.setPacketId(in.readUnsignedShort());
			}
			int stopPosition = in.readerIndex();
			//read the payload
			int payloadSize =
					remainingLength - (stopPosition - startPosition - 2) + (Utils.numBytesToEncode(remainingLength)
							- 1);
			if (in.readableBytes() < payloadSize) {
				in.resetReaderIndex();
				return;
			}
			ByteBuf byteBuffer = Unpooled.buffer(payloadSize);
			in.readBytes(byteBuffer);
			publishPacket.setPayload(byteBuffer.nioBuffer());
			out.add(publishPacket);
		}
	}
}
