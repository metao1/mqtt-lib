package com.metao.mqtt.coders.decoders;

import com.metao.mqtt.models.PacketIdMessage;
import com.metao.mqtt.models.UnsubscribeAckPacket;

/**
 * @author Mehrdad A.Karami at 3/11/19
 **/

public class UnsubscribeAckDecoder extends MessageDecoder {

	@Override
	protected PacketIdMessage createMessage() {
		return new UnsubscribeAckPacket();
	}
}
