package com.metao.mqtt.coders.decoders;

import com.metao.mqtt.models.PacketTypeMessage;
import com.metao.mqtt.models.QosType;
import com.metao.mqtt.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.util.AttributeMap;

import java.util.List;

/**
 * @author Mehrdad
 */
public abstract class Decoder {
    protected abstract void decode(AttributeMap ctx, ByteBuf in, List<Object> out) throws Exception;

    /**
     * Decodes the first 2 bytes of the MQTT packet.
     * The first byte contain the packet operation code and the flags,
     * the second byte and more contains the overall packet length.
     */
    protected boolean decodeCommonHeader(PacketTypeMessage message, ByteBuf in) {
        return genericDecodeCommonHeader(message, null, in);
    }

    /**
     * Do the same as the @see#decodeCommonHeader but having a strong validation on the flags values
     */
    protected boolean decodeCommonHeader(PacketTypeMessage message, int expectedFlags, ByteBuf in) {
        return genericDecodeCommonHeader(message, expectedFlags, in);
    }


    private boolean genericDecodeCommonHeader(PacketTypeMessage message, Integer expectedFlagsOpt, ByteBuf in) {
        //Common decoding part
        if (in.readableBytes() < 2) {
            return false;
        }
        byte h1 = in.readByte();
        byte messageType = (byte) ((h1 & 0x00F0) >> 4);

        byte flags = (byte) (h1 & 0x0F);
        if (expectedFlagsOpt != null) {
            int expectedFlags = expectedFlagsOpt;
            if ((byte) expectedFlags != flags) {
                String hexExpected = Integer.toHexString(expectedFlags);
                String hexReceived = Integer.toHexString(flags);
                throw new CorruptedFrameException(String.format("Received a message with fixed header flags (%s) != expected (%s)", hexReceived, hexExpected));
            }
        }

        boolean dupFlag = ((byte) ((h1 & 0x0008) >> 3) == 1);
        byte qosLevel = (byte) ((h1 & 0x0006) >> 1);
        boolean retainFlag = ((byte) (h1 & 0x0001) == 1);
        int remainingLength = Utils.decodeRemainingLength(in);
        if (remainingLength == -1) {
            return false;
        }

        message.setMessageType(messageType);
        message.setDupFlag(dupFlag);
        try {
            message.setQosType(QosType.valueOf(qosLevel));
        } catch (IllegalArgumentException e) {
            throw new CorruptedFrameException(String.format("Received an invalid QOS: %s", e.getMessage()), e);
        }
        message.setRetainFlag(retainFlag);
        message.setRemainingLength(remainingLength);
        return true;
    }
}
