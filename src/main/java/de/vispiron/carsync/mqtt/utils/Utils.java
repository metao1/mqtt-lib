package de.vispiron.carsync.mqtt.utils;

import de.vispiron.carsync.mqtt.coders.Decoder;
import de.vispiron.carsync.mqtt.models.PacketTypeMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.AttributeMap;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.MalformedParametersException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * @author Mehrdad A.Karami at 2/25/19
 **/

public class Utils {

	public static final byte VERSION_3_1 = 3;
	public static final byte VERSION_3_1_1 = 4;
	private static final String ATTR_USERNAME = "username";
	private static final String ATTR_SESSION_STOLEN = "sessionStolen";
	private static final String ATTR_CLIENTID = "ClientID";
	private static final String CLEAN_SESSION = "cleanSession";
	private static final String KEEP_ALIVE = "keepAlive";

	public static final AttributeKey<Object> ATTR_KEY_KEEPALIVE = AttributeKey.valueOf(KEEP_ALIVE);
	public static final AttributeKey<Object> ATTR_KEY_CLEANSESSION = AttributeKey.valueOf(CLEAN_SESSION);
	public static final AttributeKey<Object> ATTR_KEY_CLIENTID = AttributeKey.valueOf(ATTR_CLIENTID);
	public static final AttributeKey<Object> ATTR_KEY_USERNAME = AttributeKey.valueOf(ATTR_USERNAME);
	public static final AttributeKey<Object> ATTR_KEY_SESSION_STOLEN = AttributeKey.valueOf(ATTR_SESSION_STOLEN);
	private static final int MAX_LENGTH_LIMIT = 268435455;

	public static boolean isValid(String value) {
		return (value != null) && value.length() > 0;
	}

	public static String generateUniqueId() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	public static String msgTypeToString(int messageType) {
		switch (messageType) {
		case PacketTypeMessage.CONNECT:
			return "CONNECT";
		default:
			throw new RuntimeException("Unrecognizable message format" + messageType);
		}
	}

	public static boolean checkValidHeader(ByteBuf in) {
		if (in.readableBytes() < 1) {
			return false;
		}
		in.skipBytes(1);//skip the messageType
		int remainingLength = Utils.decodeRemainingLength(in);
		if (remainingLength < 0) {
			return false;
		}
		//if we have more bytes to read as the remaining length
		return in.readableBytes() >= remainingLength;
	}

	public static byte readMessageType(ByteBuf in) {
		byte header = in.readByte();
		return (byte) ((header & 0x00F0) >> 4);
	}

	/**
	 * Decode the variable remaining data if it is length or the payload
	 *
	 * @param buf byte buffer to decode
	 * @return decoded payload
	 */
	public static int decodeRemainingLength(ByteBuf buf) {
		int multiplier = 1;
		int value = 0;
		byte digit;
		do {//Read byte to byte and find out the message payload
			if (buf.readableBytes() < 1) {
				return -1;
			}
			digit = buf.readByte();//read one byte
			value += (digit & 0x7F)
					* multiplier;//8's MSB is the continues bits sign, so we want the first 7 bits and convert to integer
			multiplier *= 128;//go to next byte 1000`0000 (binary) is 128
		} while ((digit & 0x80) != 0);// Should we continue? 8's MSB is the continues bits sign
		return value;
	}

	public static byte[] convertBufferIntoArray(ByteBuf in) {
		if (in.readableBytes() < 2) {
			return null;
		}
		int strlen = in.readUnsignedShort();
		if (in.readableBytes() < strlen) {
			return null;
		}
		byte[] strRaw = new byte[strlen];
		in.readBytes(strRaw);
		return strRaw;
	}

	/**
	 * Encode the value in the format defined in the specification as variable length array
	 *
	 * @param value to decode
	 * @return encoded ByteBuffer
	 * @throws CorruptedFrameException
	 */
	public static ByteBuf encodeRemainingLength(int value) throws CorruptedFrameException { // Oh I reached here
		if (value > MAX_LENGTH_LIMIT || value < 0) {
			throw new CorruptedFrameException("Value should be in range btw 0 and " + MAX_LENGTH_LIMIT);
		}
		ByteBuf encodedByteBuffer = Unpooled.buffer(4);
		byte digit;
		do {
			digit = (byte) (value % 128);
			value = value / 128;
			if (value > 0) {
				digit = (byte) (digit | 0x80);
			}
			encodedByteBuffer.writeByte(digit);
		} while (value > 0);
		return encodedByteBuffer;
	}

	public static void sessionStolen(Channel channel, boolean value) {
		channel.attr(ATTR_KEY_SESSION_STOLEN).set(value);
	}

	public static ByteBuf encodeStringToByteBuff(String stringValue) {
		if (!isValid(stringValue)) {
			throw new MalformedParametersException("String value is a must");
		}
		byte[] raw;
		try {
			raw = stringValue.getBytes("UTF-8");
		} catch (UnsupportedEncodingException ex) {
			LoggerFactory.getLogger(Utils.class).error(null, ex);
			return null;
		}
		ByteBuf byteBuf = Unpooled.buffer(2);
		byteBuf.writeShort(raw.length);
		return byteBuf;
	}

	public static String getClientId(Channel channel) {
		return (String) channel.attr(Utils.ATTR_KEY_CLIENTID).get();
	}

	public static boolean cleanSession(Channel channel) {
		return (Boolean) channel.attr(Utils.ATTR_KEY_CLEANSESSION).get();
	}

	public static byte encodeFlags(PacketTypeMessage msg) {
		byte flags = 0;
		if (msg.isDupFlag()) {
			flags |= 0x08;
		}
		if (msg.isRetainFlag()) {
			flags |= 0x01;
		}
		flags |= ((msg.getQos().byteValue() & 0x03) << 1);
		return flags;
	}

	public static String createUniqueId() {
		return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
	}

	public static String decodeByteBuffToString(ByteBuf bytes) {
		return new String(bytes.array(), StandardCharsets.UTF_8);
	}

	public static boolean isMqttVersion3_1_1(AttributeMap map) {
		Attribute<Integer> versionAttr = map.attr(Decoder.PROTOCOL_VERSION);
		Integer protocolVersion = versionAttr.get();
		if (protocolVersion == null) {
			return true;
		}
		return protocolVersion == VERSION_3_1_1;
	}

	/**
	 * Return the number of bytes to encode the given remaining length value
	 */
	public static int numBytesToEncode(int len) {
		if (0 <= len && len <= 127) return 1;
		if (128 <= len && len <= 16383) return 2;
		if (16384 <= len && len <= 2097151) return 3;
		if (2097152 <= len && len <= 268435455) return 4;
		throw new IllegalArgumentException("value should be in the range [0..268435455]");
	}

}
