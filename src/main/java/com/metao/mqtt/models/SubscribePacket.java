package com.metao.mqtt.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Subscribe message from ODB devices
 *
 * @author Mehrdad A.Karami at 3/7/19
 **/
public class SubscribePacket extends PacketIdMessage {

	private List<DeCouple> subscriptions = new ArrayList<>();

	public SubscribePacket() {
		setMessageType(SUBSCRIBE);
		setQosType(QosType.LEAST_ONE);
	}

	/**
	 * A class to hold the information related to the type of the subscription belonging the type of the topic
	 */
	public static class DeCouple {
		private final byte qos;
		private final String topicFilter;

		public DeCouple(byte qos, String topicFilter) {
			this.qos = qos;
			this.topicFilter = topicFilter;
		}

		public byte getQos() {
			return qos;
		}

		public String getTopicFilter() {
			return topicFilter;
		}
	}

	public void addSubscriptions(DeCouple deCouple) {
		subscriptions.add(deCouple);
	}

	public List<DeCouple> getSubscriptions() {
		return subscriptions;
	}
}
