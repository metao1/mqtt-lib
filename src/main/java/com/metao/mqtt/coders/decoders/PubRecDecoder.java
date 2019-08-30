package com.metao.mqtt.coders.decoders;

import com.metao.mqtt.models.PacketIdMessage;
import com.metao.mqtt.models.PubRecPacket;

/**
 * @author Mehrdad
 */
class PubRecDecoder extends MessageIDDecoder {

    @Override
    protected PacketIdMessage createMessage() {
        return new PubRecPacket();
    }
}
