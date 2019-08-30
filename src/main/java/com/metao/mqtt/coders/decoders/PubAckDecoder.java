
package com.metao.mqtt.coders.decoders;


import com.metao.mqtt.models.PacketIdMessage;
import com.metao.mqtt.models.PublishAckPacket;

/**
 * @author Mehrdad
 */
class PubAckDecoder extends MessageDecoder {

    @Override
    protected PacketIdMessage createMessage() {
        return new PublishAckPacket();
    }

}
