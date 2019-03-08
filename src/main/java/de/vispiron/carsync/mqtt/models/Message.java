package de.vispiron.carsync.mqtt.models;

import java.util.Arrays;

/**
 * Model class for the MQTT Message
 *
 * @author Mehrdad A.Karami at 3/5/19
 **/

public class Message {

	private QosType qosType;
	private byte[] payload;
	private String topic;
	private boolean retained;
	private String clientId;
	private Integer packetId;
	private String msgId;

	public Message(byte[] message, QosType qosType, String topic) {
		this.qosType = qosType;
		this.payload = message;
		this.topic = topic;
	}

	public QosType getQosType() {
		return qosType;
	}

	public void setQosType(QosType qosType) {
		this.qosType = qosType;
	}

	public byte[] getPayload() {
		return payload;
	}

	public void setPayload(byte[] payload) {
		this.payload = payload;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public boolean isRetained() {
		return retained;
	}

	public void setRetained(boolean retained) {
		this.retained = retained;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public Integer getPacketId() {
		return packetId;
	}

	public void setPacketId(Integer packetId) {
		this.packetId = packetId;
	}

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	@Override
	public String toString() {
		return "Message{" + "qosType=" + qosType + ", payload=" + Arrays.toString(payload) + ", topic='" + topic + '\''
				+ ", retained=" + retained + ", clientId='" + clientId + '\'' + ", packetId=" + packetId + ", msgId='"
				+ msgId + '\'' + '}';
	}
}
