
package com.metao.mqtt.coders.decoders;


import com.metao.mqtt.models.PacketIdMessage;
import com.metao.mqtt.models.PublishAckPacket;

/**
 * @author Mehrdad
 */
class PubAckDecoder extends MessageIDDecoder {

    @Override
    protected PacketIdMessage createMessage() {
        return new PublishAckPacket();
    }

}
