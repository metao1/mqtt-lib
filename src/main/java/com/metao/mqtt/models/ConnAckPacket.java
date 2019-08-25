package com.metao.mqtt.models;

/**
 * @author Mehrdad A.Karami at 2/25/19
 **/

public class ConnAckPacket extends PacketTypeMessage {
	public static final byte CONNECTION_ACCEPTED = 0X00;// Connection accepted
    public static final byte UNNACEPTABLE_PROTOCOL_VERSION = 0x01;
    public static final byte IDENTIFIER_REJECTED = 0X02;// Identifier not found
	public static final byte UNACCEPTABLE_PROTOCOL_VERSION = 0x01;
    public static final byte BAD_USERNAME_OR_PASSWORD = 0x03;

    private byte returnCode;
	private boolean sessionPresent;

	public ConnAckPacket() {
		setMessageType(CONNACK);
	}

	public byte getReturnCode() {
		return returnCode;
	}
	public void setReturnCode(byte returnCode) {
		this.returnCode = returnCode;
	}
	public boolean isSessionPresent() {
		return sessionPresent;
	}

	public void setSessionPresent(boolean sessionPresent) {
		this.sessionPresent = sessionPresent;
	}
}
