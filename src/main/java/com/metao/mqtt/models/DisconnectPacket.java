package com.metao.mqtt.models;

/**
 * @author Mehrdad A.Karami at 3/11/19
 **/

public class DisconnectPacket extends ZeroLengthPacket{

	public DisconnectPacket(){
		setMessageType(DISCONNECT);
	}
}
