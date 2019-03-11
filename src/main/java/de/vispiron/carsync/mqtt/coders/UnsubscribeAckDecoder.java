package de.vispiron.carsync.mqtt.coders;

import de.vispiron.carsync.mqtt.models.UnsubscribeAckPacket;
import de.vispiron.carsync.mqtt.models.PacketIdMessage;

/**
 * @author Mehrdad A.Karami at 3/11/19
 **/

public class UnsubscribeAckDecoder extends MessageDecoder {

	@Override
	protected PacketIdMessage createMessage() {
		return new UnsubscribeAckPacket();
	}
}
