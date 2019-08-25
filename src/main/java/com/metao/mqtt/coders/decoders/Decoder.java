package com.metao.mqtt.coders.decoders;

import com.metao.mqtt.models.PacketTypeMessage;
import com.metao.mqtt.models.QosType;
import com.metao.mqtt.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.util.AttributeKey;
import io.netty.util.AttributeMap;

import java.util.List;

/**
 * @author Mehrdad A.Karami at 2/26/19
 **/

public abstract class Decoder {

    public static final AttributeKey<Integer> PROTOCOL_VERSION = AttributeKey.valueOf("version");

    protected abstract void decode(AttributeMap map, ByteBuf in, List<Object> out) throws Exception;

    /**
     * Decodes the first 2 bytes of the MQTT packet.
     * The first byte contain the packet operation code and the flags,
     * the second byte and more contains the overall packet length.
     */
    protected boolean decodeCommonHeader(PacketTypeMessage message, Integer expectedFlags, ByteBuf buf) {
        return generateHeaderPacket(message, expectedFlags, buf);
    }

    protected boolean decodeCommonHeader(PacketTypeMessage message, ByteBuf buf) {
        return generateHeaderPacket(message, null, buf);
    }

    /************************************************************************************************************/
	/*								 MQTT PACKET FORMAT
	 *  |||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 *  |     4 bits    |       4 bits    |          8 bits       |        0 - Y Bytes 		    |  0 - X Bytes  |
	 *  |  packet type  |        Flags    |      Packet Length    |   Variable Length Header    |    Payload    |
 	 *  |||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	 *  |	control packet 8 bits	      |		 8 bits at least  |												|
     *
	  	||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	    | bit   | 7   |   6   |   5  |  4  |   3   |   2   |   1   |   0   |
	    |       |                          |  Flags specific to each MQTT  |
	    | Byte1	| MQTT Control Packet Type |   	Control Packet type        |
	    |_______|__________________________|_______________________________|
	    | Byte2 |  					Remaining Length                       |
        ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||

	/************************************************************************************************************/
    private boolean generateHeaderPacket(PacketTypeMessage message, Integer expectedFlags, ByteBuf buf) {
        if (buf.readableBytes() < 2) {
            return false;
        }
        byte header = buf.readByte();
        byte messageType = (byte) ((header & 0x00F0) >> 4); // find the message type in the byte1 Header data
        byte flags = (byte) (header & 0x0F);//find if there is any flag included inside the byte1 Header data
        if (expectedFlags != null) {
            int expectedFlagsTemp = expectedFlags;
            if ((byte) expectedFlagsTemp != flags) {
                String hexExpected = Integer.toHexString(expectedFlags);
                String hexReceived = Integer.toHexString(flags);
                throw new CorruptedFrameException(String.format("Received a message with fixed header flags (%s) != expected (%s)", hexReceived, hexExpected));
            }
        }
        boolean dupFlag = ((byte) ((header & 0x0008) >> 3) == 1);//Extra Flags enabled?
        byte qosLevel = (byte) ((header & 0x0006) >> 1);//Find the message QoS level
        boolean retainFlag = ((byte) (header & 0x0001) == 1);//Is the message continues?
        int packetLength = Utils.decodeRemainingLength(buf);//Decode the payload
        if (packetLength == -1) {// We don't accept empty packs
            return false;//At least the MQTT packet size is 2 bytes
        }
        message.setMessageType(messageType);
        message.setDupFlag(dupFlag);
        try {
            message.setQosType(QosType.valueOf(qosLevel));
        } catch (IllegalArgumentException e) {
            throw new CorruptedFrameException(String.format("Received an invalid QoS: %s", e.getMessage()), e);
        }
        message.setRetainFlag(retainFlag);
        message.setRemainingLength(packetLength);
        return true;
    }
}
