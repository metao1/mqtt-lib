package de.vispiron.carsync.mqtt.models;

import java.nio.ByteBuffer;

/**
 * @author Mehrdad A.Karami at 3/7/19
 **/

public class WillMessage {
	private final String topic;
	private final ByteBuffer payload;
	private final boolean retained;
	private final  QosType qosType;

	public WillMessage(String topic, ByteBuffer payload, boolean retained, QosType qosType) {
		this.topic = topic;
		this.payload = payload;
		this.retained = retained;
		this.qosType = qosType;
	}

	public String getTopic() {
		return topic;
	}

	public ByteBuffer getPayload() {
		return payload;
	}

	public boolean isRetained() {
		return retained;
	}

	public QosType getQosType() {
		return qosType;
	}
}
