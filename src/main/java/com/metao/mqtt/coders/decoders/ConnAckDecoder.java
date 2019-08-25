package com.metao.mqtt.coders.decoders;

import com.metao.mqtt.models.ConnAckPacket;
import io.netty.buffer.ByteBuf;
import io.netty.util.AttributeMap;

import java.util.List;

/**
 * @author Mehrdad A.Karami at 2/28/19
 **/

public class ConnAckDecoder extends Decoder {

    @Override
    protected void decode(AttributeMap map, ByteBuf in, List<Object> out) throws Exception {
        in.resetReaderIndex();
        //Common decoding part
        ConnAckPacket connAckPacket = new ConnAckPacket();
        if (!decodeCommonHeader(connAckPacket, in)) {
            in.resetReaderIndex();
            return;
        }
        in.skipBytes(1);//skip reserved byte
        //read return code
        connAckPacket.setReturnCode(in.readByte());
        out.add(connAckPacket);
    }
}
