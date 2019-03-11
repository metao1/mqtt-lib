package de.vispiron.carsync.mqtt.models;

/**
 * @author Mehrdad A.Karami at 3/11/19
 **/

public class PingResponsePacket extends ZeroLengthPacket {

	public PingResponsePacket() {
		setMessageType(PING_RESPONSE);
	}
}
