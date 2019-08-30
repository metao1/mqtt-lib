package com.metao.mqtt.coders.decoders;

import com.metao.mqtt.models.ConnAckPacket;
import io.netty.buffer.ByteBuf;
import io.netty.util.AttributeMap;

import java.util.List;

/**
 * @author Mehrdad
 */
class ConnAckDecoder extends Decoder {

    @Override
    protected void decode(AttributeMap ctx, ByteBuf in, List<Object> out) throws Exception {
        in.resetReaderIndex();
        //Common decoding part
        ConnAckPacket message = new ConnAckPacket();
        if (!decodeCommonHeader(message, 0x00, in)) {
            in.resetReaderIndex();
            return;
        }
        //skip reserved byte
        in.skipBytes(1);

        //read  return code
        message.setReturnCode(in.readByte());
        out.add(message);
    }

}
