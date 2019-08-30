
package com.metao.mqtt.coders.decoders;

import com.metao.mqtt.models.PingRequestPacket;
import io.netty.buffer.ByteBuf;
import io.netty.util.AttributeMap;

import java.util.List;

/**
 * @author Mehrdad
 */
class PingReqDecoder extends Decoder {

    @Override
    protected void decode(AttributeMap ctx, ByteBuf in, List<Object> out) throws Exception {
        //Common decoding part
        in.resetReaderIndex();
        PingRequestPacket message = new PingRequestPacket();
        if (!decodeCommonHeader(message, 0x00, in)) {
            in.resetReaderIndex();
            return;
        }
        out.add(message);
    }
}
