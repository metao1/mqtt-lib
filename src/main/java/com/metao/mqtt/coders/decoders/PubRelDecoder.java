package com.metao.mqtt.coders.decoders;

import com.metao.mqtt.models.PubRelPacket;
import com.metao.mqtt.models.PacketIdMessage;
import io.netty.buffer.ByteBuf;
import io.netty.util.AttributeMap;

import java.util.List;

public class PubRelDecoder extends Decoder {
    @Override
    protected void decode(AttributeMap map, ByteBuf in, List<Object> out) throws Exception {
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
