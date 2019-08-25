package com.metao.mqtt.models;

import java.util.ArrayList;
import java.util.List;

public class SubscribeAckPacket extends PacketIdMessage {

    private List<QosType> types = new ArrayList<>();

    public SubscribeAckPacket() {
        setMessageType(SUBACK);
        setQosType(QosType.LEAST_ONE);
    }

    public List<QosType> types() {
        return types;
    }

    public void addType(QosType type) {
        types.add(type);
    }
}
