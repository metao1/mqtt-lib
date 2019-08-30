package com.metao.mqtt.coders.decoders;

import com.metao.mqtt.models.DisconnectPacket;
import io.netty.buffer.ByteBuf;
import io.netty.util.AttributeMap;

import java.util.List;

/**
 * @author Mehrdad
 */
class DisconnectDecoder extends Decoder {

    @Override
    protected void decode(AttributeMap ctx, ByteBuf in, List<Object> out) throws Exception {
        //Common decoding part
        in.resetReaderIndex();
        DisconnectPacket message = new DisconnectPacket();
        if (!decodeCommonHeader(message, 0x00, in)) {
            in.resetReaderIndex();
            return;
        }
        out.add(message);
    }

}
