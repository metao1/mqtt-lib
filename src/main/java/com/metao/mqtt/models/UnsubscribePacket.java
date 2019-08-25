package com.metao.mqtt.models;

public class UnsubscribePacket extends PacketIdMessage{

    public UnsubscribePacket() {
        setMessageType(SUBSCRIBE);
    }
}
