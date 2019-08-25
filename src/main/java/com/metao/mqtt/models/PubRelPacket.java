package com.metao.mqtt.models;

public class PubRelPacket extends PacketIdMessage{

    public PubRelPacket() {
        setMessageType(PUBREL);
    }
}
