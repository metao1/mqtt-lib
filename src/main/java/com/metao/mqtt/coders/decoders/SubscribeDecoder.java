package com.metao.mqtt.coders.decoders;

import com.metao.mqtt.models.QosType;
import com.metao.mqtt.models.SubscribePacket;
import com.metao.mqtt.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.util.AttributeMap;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * @author Mehrdad
 */
class SubscribeDecoder extends Decoder {

    @Override
    protected void decode(AttributeMap ctx, ByteBuf in, List<Object> out) throws Exception {
        //Common decoding part
        SubscribePacket message = new SubscribePacket();
        in.resetReaderIndex();
        if (!decodeCommonHeader(message, 0x02, in)) {
            in.resetReaderIndex();
            return;
        }

        //check qos level
        if (message.getQos() != QosType.LEAST_ONE) {
            throw new CorruptedFrameException("Received SUBSCRIBE message with QoS other than LEAST_ONE, was: " + message.getQos());
        }

        int start = in.readerIndex();
        //read  messageIDs
        message.setPacketId(in.readUnsignedShort());
        int read = in.readerIndex() - start;
        while (read < message.getRemainingLength()) {
            decodeSubscription(in, message);
            read = in.readerIndex() - start;
        }

        if (message.getSubscriptions().isEmpty()) {
            throw new CorruptedFrameException("subscribe MUST have got at least 1 couple topic/QoS");
        }

        out.add(message);
    }

    /**
     * Populate the message with couple of Qos, topic
     */
    private void decodeSubscription(ByteBuf in, SubscribePacket message) throws UnsupportedEncodingException {
        String topic = Utils.decodeByteBuffToString(in);
        //check topic is at least one char [MQTT-4.7.3-1]
        if (topic.length() == 0) {
            throw new CorruptedFrameException("Received a SUBSCRIBE with empty topic filter");
        }
        byte qosByte = in.readByte();
        if ((qosByte & 0xFC) > 0) { //the first 6 bits is reserved => has to be 0
            throw new CorruptedFrameException("subscribe MUST have QoS byte with reserved buts to 0, found " + Integer.toHexString(qosByte));
        }
        byte qos = (byte) (qosByte & 0x03);
        //TODO check qos id 000000xx
        message.addSubscriptions(new SubscribePacket.DeCouple(qos, topic));
    }

}
