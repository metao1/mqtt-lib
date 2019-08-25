package com.metao.mqtt.coders.decoders;

import com.metao.mqtt.models.PacketIdMessage;
import io.netty.buffer.ByteBuf;
import io.netty.util.AttributeMap;

import java.util.List;

/**
 * @author Mehrdad A.Karami at 3/11/19
 **/

public abstract class MessageDecoder extends Decoder {

	protected abstract PacketIdMessage createMessage();

    @Override
    protected void decode(AttributeMap map, ByteBuf in, List<Object> out) throws Exception {
        in.resetReaderIndex();
        //Common decoding part
        PacketIdMessage packetIdMessage = createMessage();
        if (!decodeCommonHeader(packetIdMessage, 0X00, in)) {
            return;
        }
        //read messageIDs
        packetIdMessage.setPacketId(in.readUnsignedShort());
        out.add(packetIdMessage);
    }
}
