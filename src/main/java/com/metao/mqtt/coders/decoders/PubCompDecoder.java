package com.metao.mqtt.coders.decoders;

import com.metao.mqtt.models.PubCompPacket;
import com.metao.mqtt.models.PacketIdMessage;

public class PubCompDecoder extends MessageDecoder {

    @Override
    protected PacketIdMessage createMessage() {
        return new PubCompPacket();
    }
}
