package com.metao.mqtt.models;

public class PubRecPacket extends PacketIdMessage{

    public PubRecPacket() {
        setMessageType(PUBREC);
    }
}
