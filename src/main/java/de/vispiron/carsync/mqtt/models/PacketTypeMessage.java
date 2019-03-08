package de.vispiron.carsync.mqtt.models;

import org.springframework.lang.NonNull;

/**
 * @author Mehrdad A.Karami at 2/25/19
 **/

public class PacketTypeMessage {
	public static final byte CONNECT = 1;//Client request to connect to server
	public static final byte CONNACK = 2;//Client Acknowledgement
	public static final byte PUBLISH = 3;//Client published a message
	public static final byte PUBACK = 4;// Publish Acknowledgement
	public static final byte PUBREC = 5;// Publish received by the server
	public static final byte SUBSCRIBE = 6; //Client Subscribe
	public static final byte UNSUBSCRIBE = 7; // Client unsubscribe
	public static final byte DISCONNECT = 8;// Client Disconnects
	public static final byte SUBSACK = 9;// Client Subscribe Acknowledgement


	//Common Packet flags
	private boolean dupFlag;
	private boolean retainFlag;

	//packet length
	private int packetLength;

	//packet qos type
	private QosType qosType;

	//public values
	private byte messageType;

	public void setMessageType(
			@NonNull
					byte messageType) {
		this.messageType = messageType;
	}
	public QosType getQosType() {
		return qosType;
	}

	public void setQosType(QosType qosType) {
		this.qosType = qosType;
	}

	public byte getMessageType() {
		return this.messageType;
	}

	public void setQos(QosType qosType) {
		this.qosType = qosType;
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
}
