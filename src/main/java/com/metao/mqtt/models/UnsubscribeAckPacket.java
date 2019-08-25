package com.metao.mqtt.models;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mehrdad A.Karami at 3/11/19
 **/

public class UnsubscribeAckPacket extends PacketIdMessage {

    private List<String> types = new ArrayList<>();

	public UnsubscribeAckPacket() {
		setMessageType(UNSUBACK);
	}

    public List<String> topicFilters() {
        return types;
    }

    public void addTopicFilter(String type) {
        types.add(type);
    }
}
