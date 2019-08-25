package com.metao.mqtt.coders.decoders;

import com.metao.mqtt.models.PacketIdMessage;
import com.metao.mqtt.models.PublishAckPacket;

public class PublishAckDecoder extends MessageDecoder {

    @Override
    protected PacketIdMessage createMessage() {
        return new PublishAckPacket();
    }
}
