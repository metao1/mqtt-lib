package com.metao.mqtt.models;

public class PublishReceivePacket extends PacketIdMessage{

    public PublishReceivePacket() {
        setMessageType(PUBREC);
    }
}
