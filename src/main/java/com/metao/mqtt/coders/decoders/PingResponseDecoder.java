package com.metao.mqtt.coders.decoders;

import com.metao.mqtt.models.PingResponsePacket;
import io.netty.buffer.ByteBuf;
import io.netty.util.AttributeMap;

import java.util.List;

/**
 * @author Mehrdad A.Karami at 3/11/19
 **/

public class PingResponseDecoder extends Decoder {

    @Override
    protected void decode(AttributeMap map, ByteBuf in, List<Object> out) throws Exception {
        in.resetReaderIndex();
        PingResponsePacket pingResponsePacket = new PingResponsePacket();
        if (!decodeCommonHeader(pingResponsePacket, 0x00, in)) {
            in.resetReaderIndex();
            return;
        }
        out.add(pingResponsePacket);
    }
}
