
package com.metao.mqtt.coders.decoders;


import com.metao.mqtt.models.PacketIdMessage;
import com.metao.mqtt.models.PubCompPacket;

/**
 * @author Mehrdad
 */
class PubCompDecoder extends MessageDecoder {

    @Override
    protected PacketIdMessage createMessage() {
        return new PubCompPacket();
    }
}
