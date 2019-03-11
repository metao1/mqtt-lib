package de.vispiron.carsync.mqtt.models;

/**
 * @author Mehrdad A.Karami at 3/11/19
 **/

public class PingRequestPacket extends ZeroLengthPacket{

	public PingRequestPacket(){
		setMessageType(PING_REQUEST);
	}
}
