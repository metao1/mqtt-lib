package com.metao.mqtt.coders.decoders;

import com.metao.mqtt.models.PingRequestPacket;
import io.netty.buffer.ByteBuf;
import io.netty.util.AttributeMap;

import java.util.List;

public class PingReqDecoder extends Decoder {

    @Override
    protected void decode(AttributeMap map, ByteBuf in, List<Object> out) {
        in.resetReaderIndex();
        PingRequestPacket message = new PingRequestPacket();
        if (!decodeCommonHeader(message, 0x00, in)) {
            in.resetReaderIndex();
            return;
        }
        out.add(message);
    }
}
