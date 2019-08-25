package com.metao.mqtt.models;

import com.metao.mqtt.coders.decoders.Decoder;
import com.metao.mqtt.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.util.AttributeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Mehrdad A.Karami at 3/11/19
 **/

public class PublishDecoder extends Decoder {
    Logger LOG = LoggerFactory.getLogger(PublishDecoder.class);

    @Override
    protected void decode(AttributeMap map, ByteBuf in, List<Object> out) throws Exception {
        LOG.debug("decode invoked with buffer {}", in);
        in.resetReaderIndex();
        int startPos = in.readerIndex();

        //Common decoding part
        PublishPacket message = new PublishPacket();
        if (!decodeCommonHeader(message, in)) {
            LOG.debug("decode ask for more data after {}", in);
            in.resetReaderIndex();
            return;
        }

        if (Utils.isMQTT3_1_1(map)) {
            if (message.getQos() == QosType.MOST_ONE && message.isDupFlag()) {
                //bad protocol, if QoS=0 => DUP = 0
                throw new CorruptedFrameException("Received a PUBLISH with QoS=0 & DUP = 1, MQTT 3.1.1 violation");
            }

            if (message.getQos() == QosType.RESERVED) {
                throw new CorruptedFrameException("Received a PUBLISH with QoS flags setted 10 b11, MQTT 3.1.1 violation");
            }
        }

        int remainingLength = message.getRemainingLength();

        //Topic name
        String topic = Utils.decodeByteBuffToString(in);
        if (topic == null) {
            in.resetReaderIndex();
            return;
        }
        //[MQTT-3.3.2-2] The Topic Name in the PUBLISH Packet MUST NOT contain wildcard characters.
        if (topic.contains("+") || topic.contains("#")) {
            throw new CorruptedFrameException("Received a PUBLISH with topic containing wild card chars, topic: " + topic);
        }
        //check topic is at least one char [MQTT-4.7.3-1]
        if (topic.length() == 0) {
            throw new CorruptedFrameException("Received a PUBLISH with topic without any character");
        }

        message.setTopicName(topic);

        if (message.getQos() == QosType.LEAST_ONE ||
            message.getQos() == QosType.EXACTLY_ONCE) {
            message.setPacketId(in.readUnsignedShort());
        }
        int stopPos = in.readerIndex();

        //read the payload
        int payloadSize = remainingLength - (stopPos - startPos - 2) + (Utils.numBytesToEncode(remainingLength) - 1);
        if (in.readableBytes() < payloadSize) {
            in.resetReaderIndex();
            return;
        }
        ByteBuf bb = Unpooled.buffer(payloadSize);
        in.readBytes(bb);
        message.setPayload(bb.nioBuffer());

        out.add(message);
    }
}
