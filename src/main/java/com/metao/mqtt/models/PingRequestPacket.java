package com.metao.mqtt.models;

/**
 * @author Mehrdad A.Karami at 3/11/19
 **/

public class PingRequestPacket extends ZeroLengthPacket{

	public PingRequestPacket(){
		setMessageType(PINGREQ);
	}
}
