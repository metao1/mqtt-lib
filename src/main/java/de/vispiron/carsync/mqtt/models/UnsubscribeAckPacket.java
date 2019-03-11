package de.vispiron.carsync.mqtt.models;

/**
 * @author Mehrdad A.Karami at 3/11/19
 **/

public class UnsubscribeAckPacket extends PacketIdMessage {

	public UnsubscribeAckPacket() {
		setMessageType(UNSUBACK);
	}
}
