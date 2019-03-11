package de.vispiron.carsync.mqtt.models;

import java.nio.ByteBuffer;

/**
 * @author Mehrdad A.Karami at 3/11/19
 **/

public class PublishPacket extends PacketIdMessage {

	private boolean local = true;
	private String clientId;
	protected String topicName;
	protected ByteBuffer payload;

	public PublishPacket() {
		setPacketLength(PUBLISH);
	}

	public String getTopicName() {
		return topicName;
	}

	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientId() {
		return clientId;
	}

	public void setPayload(ByteBuffer payload) {
		this.payload = payload;
	}

	public ByteBuffer getPayload() {
		return payload;
	}
}
