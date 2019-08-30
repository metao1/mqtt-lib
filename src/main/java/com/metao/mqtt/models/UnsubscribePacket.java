package com.metao.mqtt.models;

import java.util.ArrayList;
import java.util.List;

public class UnsubscribePacket extends PacketIdMessage {

    private List<String> types = new ArrayList<String>();

    public UnsubscribePacket() {
        setMessageType(SUBSCRIBE);
    }

    public List<String> topicFilters() {
        return types;
    }

    public void addTopicFilter(String type) {
        types.add(type);
    }
}
