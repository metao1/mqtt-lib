
package com.metao.mqtt.coders.decoders;

import com.metao.mqtt.models.PacketIdMessage;
import com.metao.mqtt.models.PubRelPacket;
import io.netty.buffer.ByteBuf;
import io.netty.util.AttributeMap;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * @author Mehrdad
 */
class PubRelDecoder extends Decoder {

    @Override
    protected void decode(AttributeMap ctx, ByteBuf in, List<Object> out) throws UnsupportedEncodingException {
        in.resetReaderIndex();
        //Common decoding part
        PacketIdMessage message = new PubRelPacket();
        if (!decodeCommonHeader(message, 0x02, in)) {
            in.resetReaderIndex();
            return;
        }

        //read  messageIDs
        message.setPacketId(in.readUnsignedShort());
        out.add(message);
    }

}

