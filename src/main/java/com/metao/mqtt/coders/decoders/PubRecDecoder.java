package com.metao.mqtt.coders.decoders;

import com.metao.mqtt.models.PubRecPacket;
import com.metao.mqtt.models.PacketIdMessage;

public class PubRecDecoder extends MessageDecoder {

    @Override
    protected PacketIdMessage createMessage() {
        return new PubRecPacket();
    }
}
