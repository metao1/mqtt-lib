package com.metao.mqtt.coders.decoders;

import com.metao.mqtt.models.PacketIdMessage;
import io.netty.buffer.ByteBuf;
import io.netty.util.AttributeMap;

import java.util.List;

/**
 * @author Mehrdad
 */
abstract class MessageIDDecoder extends Decoder {

    protected abstract PacketIdMessage createMessage();

    @Override
    protected void decode(AttributeMap ctx, ByteBuf in, List<Object> out) throws Exception {
        in.resetReaderIndex();
        //Common decoding part
        PacketIdMessage message = createMessage();
        if (!decodeCommonHeader(message, 0x00, in)) {
            in.resetReaderIndex();
            return;
        }

        //read  messageIDs
        message.setPacketId(in.readUnsignedShort());
        out.add(message);
    }

}
