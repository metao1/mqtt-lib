package de.vispiron.carsync.mqtt.models;

/**
 * Model class that maintains information about which Topic a certain ClientID is subscribed
 *
 * @author Mehrdad A.Karami at 3/5/19
 **/

public class Subscription {
	final QosType requestedQos;
	final String clientId;
	final String topicFilter;

	public Subscription(String clientId, String topicFilter, QosType requestedQos) {
		this.requestedQos = requestedQos;
		this.clientId = clientId;
		this.topicFilter = topicFilter;
	}

	Subscription(Subscription copy) {
		this.requestedQos = copy.requestedQos;
		this.clientId = copy.clientId;
		this.topicFilter = copy.topicFilter;
	}

	public QosType getRequestedQos() {
		return requestedQos;
	}

	public String getClientId() {
		return clientId;
	}

	public String getTopicFilter() {
		return topicFilter;
	}
}
