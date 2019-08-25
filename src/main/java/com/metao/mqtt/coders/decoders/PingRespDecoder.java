package com.metao.mqtt.coders.decoders;

import com.metao.mqtt.models.PingResponsePacket;
import io.netty.buffer.ByteBuf;
import io.netty.util.AttributeMap;

import java.util.List;

public class PingRespDecoder extends Decoder {

    @Override
    protected void decode(AttributeMap map, ByteBuf in, List<Object> out) {
        in.resetReaderIndex();
        PingResponsePacket message = new PingResponsePacket();
        if (!decodeCommonHeader(message, 0x00, in)) {
            in.resetReaderIndex();
            return;
        }
        out.add(message);
    }
}
