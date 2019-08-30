package com.metao.mqtt.models;

import org.springframework.lang.NonNull;

/**
 * @author Mehrdad A.Karami at 2/25/19
 **/

public class PacketTypeMessage {

    public static final byte CONNECT = 1; // Client request to connect to Server
    public static final byte CONNACK = 2; // Connect Acknowledgment
    public static final byte PUBLISH = 3; // Publish message
    public static final byte PUBACK = 4; // Publish Acknowledgment
    public static final byte PUBREC = 5; //Publish Received (assured delivery part 1)
    public static final byte PUBREL = 6; // Publish Release (assured delivery part 2)
    public static final byte PUBCOMP = 7; //Publish Complete (assured delivery part 3)
    public static final byte SUBSCRIBE = 8; //Client Subscribe request
    public static final byte SUBACK = 9; // Subscribe Acknowledgment
    public static final byte UNSUBSCRIBE = 10; //Client Unsubscribe request
    public static final byte UNSUBACK = 11; // Unsubscribe Acknowledgment
    public static final byte PINGREQ = 12; //PING Request
    public static final byte PINGRESP = 13; //PING Response
    public static final byte DISCONNECT = 14; //Client is Disconnecting


    //Common Packet flags
	private boolean dupFlag;
	private boolean retainFlag;

	//packet length
	private int packetLength;

	//packet qos type
	private QosType qosType;

	//public values
	private byte messageType;

	protected int remainingLength;

	public void setMessageType(@NonNull byte messageType) {
		this.messageType = messageType;
	}

	public void setQosType(QosType qosType) {
		this.qosType = qosType;
	}

	public byte getMessageType() {
		return this.messageType;
	}

	public void setDupFlag(boolean dupFlag) {
		this.dupFlag = dupFlag;
	}

	public void setRetainFlag(boolean retainFlag) {
		this.retainFlag = retainFlag;
	}
	public QosType getQos() {
		return qosType;
	}

	public boolean isRetainFlag() {
		return retainFlag;
	}

	public boolean isDupFlag() {
		return dupFlag;
	}

	public void setPacketLength(int packetLength) {
		this.packetLength = packetLength;
	}

	public int getPacketLength() {
		return packetLength;
	}

    public int getRemainingLength() {
        return remainingLength;
    }

    public void setRemainingLength(int remainingLength) {
        this.remainingLength = remainingLength;
    }
}
