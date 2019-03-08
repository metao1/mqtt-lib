package de.vispiron.carsync.mqtt.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Subscribe Acknowledge message
 *
 * @author Mehrdad A.Karami at 3/7/19
 **/
//todo complete the Subscribe Acknowledge message
public class SubscribeAckMessage extends PacketIdMessage {
	private List<QosType> types = new ArrayList<>();

	public SubscribeAckMessage(){
		setMessageType(PacketTypeMessage.SUBSCRIBE);
	}

	public void addType(QosType qosType) {
		this.types.add(qosType);
	}

	public List<QosType> getTypes() {
		return types;
	}
}
