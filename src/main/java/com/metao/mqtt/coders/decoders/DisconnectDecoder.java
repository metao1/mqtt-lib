package com.metao.mqtt.coders.decoders;

import com.metao.mqtt.models.DisconnectPacket;
import io.netty.buffer.ByteBuf;
import io.netty.util.AttributeMap;

import java.util.List;

/**
 * @author Mehrdad A.Karami at 3/11/19
 **/

public class DisconnectDecoder extends Decoder {
    @Override
    protected void decode(AttributeMap map, ByteBuf in, List<Object> out) throws Exception {
        in.resetReaderIndex();
        DisconnectPacket disconnectPacket = new DisconnectPacket();
        if (!decodeCommonHeader(disconnectPacket, 0x00, in)) {
            in.resetReaderIndex();
            return;
        }
        out.add(disconnectPacket);
    }
}
