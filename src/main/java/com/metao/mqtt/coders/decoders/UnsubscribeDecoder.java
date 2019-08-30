
package com.metao.mqtt.coders.decoders;

import com.metao.mqtt.models.QosType;
import com.metao.mqtt.models.UnsubscribePacket;
import com.metao.mqtt.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.util.AttributeMap;

import java.util.List;

/**
 * @author Mehrdad
 */
class UnsubscribeDecoder extends Decoder {

    @Override
    protected void decode(AttributeMap ctx, ByteBuf in, List<Object> out) throws Exception {
        //Common decoding part
        in.resetReaderIndex();
        UnsubscribePacket message = new UnsubscribePacket();
        if (!decodeCommonHeader(message, 0x02, in)) {
            in.resetReaderIndex();
            return;
        }

        //check qos level
        if (message.getQos() != QosType.LEAST_ONE) {
            throw new CorruptedFrameException("Found an Unsubscribe message with qos other than LEAST_ONE, was: " + message.getQos());
        }

        int start = in.readerIndex();
        //read  messageIDs
        message.setPacketId(in.readUnsignedShort());
        int read = in.readerIndex() - start;
        while (read < message.getRemainingLength()) {
            String topicFilter = Utils.decodeByteBuffToString(in);
            //check topic is at least one char [MQTT-4.7.3-1]
            if (topicFilter.length() == 0) {
                throw new CorruptedFrameException("Received an UNSUBSCRIBE with empty topic filter");
            }
            message.addTopicFilter(topicFilter);
            read = in.readerIndex() - start;
        }
        if (message.topicFilters().isEmpty()) {
            throw new CorruptedFrameException("unsubscribe MUST have got at least 1 topic");
        }
        out.add(message);
    }

}
