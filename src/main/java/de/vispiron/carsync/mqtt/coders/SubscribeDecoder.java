package de.vispiron.carsync.mqtt.coders;

import de.vispiron.carsync.mqtt.models.QosType;
import de.vispiron.carsync.mqtt.models.SubscribeMessage;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.util.AttributeMap;

import java.util.List;

/**
 * @author Mehrdad A.Karami at 3/8/19
 **/

public class SubscribeDecoder extends Decoder {

	@Override
	void decode(AttributeMap map, ByteBuf in, List<Object> out) throws Exception {
		//Common decoding part
		in.resetReaderIndex();
		SubscribeMessage subscribeMessage = new SubscribeMessage();
		if (!decodeCommonHeader(subscribeMessage, 0x02, in)) {
			in.resetReaderIndex();
			return;
		}
		//check qos level
		if (subscribeMessage.getQos() != QosType.LEAST_ONE) {
			throw new CorruptedFrameException(
					"Received SUBSCRIBE message with QoS other than LEAST_ONE, was :" + subscribeMessage.getQos());
		}
	}

}
