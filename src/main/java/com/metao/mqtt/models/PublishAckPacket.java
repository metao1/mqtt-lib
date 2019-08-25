package com.metao.mqtt.models;

public class PublishAckPacket extends PacketIdMessage {

    public PublishAckPacket() {
        setMessageType(PUBACK);
    }
}
