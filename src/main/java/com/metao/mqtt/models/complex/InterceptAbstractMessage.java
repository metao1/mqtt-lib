package com.metao.mqtt.models.complex;

import com.metao.mqtt.models.PacketTypeMessage;
import com.metao.mqtt.models.QosType;

public abstract class InterceptAbstractMessage {
    private final PacketTypeMessage msg;

    InterceptAbstractMessage(PacketTypeMessage msg) {
        this.msg = msg;
    }

    public boolean isRetainFlag() {
        return msg.isRetainFlag();
    }

    public boolean isDupFlag() {
        return msg.isDupFlag();
    }

    public QosType getQos() {
        return msg.getQos();
    }
}
