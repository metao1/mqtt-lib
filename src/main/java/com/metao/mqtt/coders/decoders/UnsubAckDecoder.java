package com.metao.mqtt.coders.decoders;


import com.metao.mqtt.models.PacketIdMessage;
import com.metao.mqtt.models.UnsubscribeAckPacket;

/**
 * @author Mehrdad
 */
class UnsubAckDecoder extends MessageDecoder {

    @Override
    protected PacketIdMessage createMessage() {
        return new UnsubscribeAckPacket();
    }
}

