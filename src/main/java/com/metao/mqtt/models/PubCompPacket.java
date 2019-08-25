package com.metao.mqtt.models;

public class PubCompPacket extends PacketIdMessage {

    public PubCompPacket() {
        setMessageType(PUBCOMP);
    }
}
