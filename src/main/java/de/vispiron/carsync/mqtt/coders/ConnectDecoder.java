package de.vispiron.carsync.mqtt.coders;

import de.vispiron.carsync.mqtt.models.ConnectPacket;
import de.vispiron.carsync.mqtt.models.PacketTypeMessage;
import de.vispiron.carsync.mqtt.models.QosType;
import de.vispiron.carsync.mqtt.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.util.Attribute;
import io.netty.util.AttributeMap;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * @author Mehrdad A.Karami at 2/27/19
 **/

public class ConnectDecoder extends Decoder {

	@Override
	void decode(AttributeMap map, ByteBuf in, List<Object> out) throws Exception {
		in.resetReaderIndex();
		//Common decoding part
		ConnectPacket connectPacket = new ConnectPacket();
		if (!decodeCommonHeader(connectPacket, in)) {
			in.resetReaderIndex();
			return;
		}
		int remainingLength = connectPacket.getPacketLength();
		int start = in.readerIndex();
		int protocolNameLength = in.readUnsignedShort();//Returns the unsigned short value at the current reader-
		//index as an int and increases the readerIndex by 2.

		byte[] encProtocolName;
		Attribute<Integer> versionAttribute = map.attr(Decoder.PROTOCOL_VERSION);
		switch (protocolNameLength) {
		case 6: { // MQTT version 3.1 "MQIsdp which is 6 or 8 butes"
			if (in.readableBytes() < 10) {
				in.resetReaderIndex();
				return;
			}
			encProtocolName = new byte[6];
			in.readBytes(encProtocolName);
			setAndCheckProtocolName(encProtocolName, in, connectPacket);
			versionAttribute.set((int) Utils.VERSION_3_1);
			break;
		}
		case 4: { // MQTT version 3.1 "MQIsdp which is 6 or 8 butes"
			if (in.readableBytes() < 8) {
				in.resetReaderIndex();
				return;
			}
			encProtocolName = new byte[4];
			in.readBytes(encProtocolName);
			setAndCheckProtocolName(encProtocolName, in, connectPacket);
			versionAttribute.set((int) Utils.VERSION_3_1_1);
			break;
		}
		default:
			throw new CorruptedFrameException("Invalid protocol size!: " + protocolNameLength);
		}
		//Protocol Version is 1 byte (value 0x03 for 3.1, 0x04 for 3.1.1)
		connectPacket.setProtocolVersion(in.readByte());
		if (connectPacket.getProtocolVersion() == Utils.VERSION_3_1_1) {
			if (connectPacket.isDupFlag() || connectPacket.isRetainFlag()
					|| connectPacket.getQos() != QosType.MOST_ONE) {
				throw new CorruptedFrameException("Received a Connect with fixed header flags != 0");
			}
			//todo check if the same session reconnect and block it
			// todo open a session after connection with the pack

		}
		byte connFlag = in.readByte();
		if (connectPacket.getProtocolVersion() == Utils.VERSION_3_1_1) {
			if ((connFlag & 0x01) != 0) {//gets the first bit and check if the flag is set
				throw new CorruptedFrameException("Received a CONNECT with connection flag [0 bit] != 0");
			}
		}
		//Page 21 on https://confluence.cardev.de/display/DC/MQTT+version+3.1.1+Protocol
		/*******************************************************************************
		 *  Bit |  7             |  6            |  5          |  4       |  3       |  2           |  1          |  0      |
		 *  	  user name flag   Password Flag   Will Retain   Will QoS   			Will Flag    Clean Session  Reserved
		 * byte 8|
		 *******************************************************************************/
		//calculate the flags!
		boolean cleanSession = ((connFlag & 0x02) >> 1) == 1;
		boolean willFlag = ((connFlag & 0x04) >> 2) == 1;
		byte willQos = (byte) ((connFlag & 0x18) >> 3);
		boolean willRetainFlag = ((connFlag & 0x20) >> 5) == 1;
		boolean passwordFlag = ((connFlag & 0x40) >> 6) == 1;
		boolean userFlag = ((connFlag & 0x080) >> 7) == 1;
		if (!userFlag && passwordFlag) {// Password without user does not mean anything
			in.resetReaderIndex();
			throw new CorruptedFrameException(
					"Expected password flag to true if the user flag was true but is: " + passwordFlag);
		}
		connectPacket.setCleanSession(cleanSession);
		connectPacket.setWillFlag(willFlag);
		connectPacket.setWillQos(willQos);
		connectPacket.setPasswordFlag(passwordFlag);
		connectPacket.setWillRetainFlag(willRetainFlag);
		connectPacket.setUserFlag(userFlag);
		//read next
		int keepAlive = in.readUnsignedShort();//Returns the unsigned short value at the current reader-
		//index as an int and increases the readerIndex by 2.
		connectPacket.setKeepAlive(keepAlive);
		if ((remainingLength == 2 && connectPacket.getProtocolVersion() == Utils.VERSION_3_1) || (remainingLength == 10
				&& connectPacket.getProtocolVersion() == Utils.VERSION_3_1_1)) {
			out.add(connectPacket);
			return;
		}
		//decode the pack id
		String clientId = new String(Utils.convertBufferIntoArray(in), "UTF-8");
		if (clientId == null) {
			in.resetReaderIndex();
			return;
		}
		connectPacket.setClientId(clientId);
		if (willFlag) {//if there is a will flag set
			String willTopic = new String(Utils.convertBufferIntoArray(in), "UTF-8");
			if (willTopic == null) {
				in.resetReaderIndex();
				return;
			}
			connectPacket.setWillTopic(willTopic);
		}
		int readed = in.readerIndex() - start;
		if (readed == remainingLength) {
			out.add(connectPacket);
			return;
		}
		if (userFlag) {
			String userName = new String(Utils.convertBufferIntoArray(in), "UTF-8");
			if (userName == null) {
				in.resetReaderIndex();
				return;
			}
			connectPacket.setUserName(userName);
		}
		readed = in.readerIndex() - start;
		if (readed == in.readerIndex()) {
			out.add(connectPacket);
		}
		if (passwordFlag) {//decode the password
			byte[] password = Utils.convertBufferIntoArray(in);
			if (password == null) {
				in.resetReaderIndex();
				return;
			}
			connectPacket.setPassword(password);
		}
		out.add(connectPacket);
	}

	private void setAndCheckProtocolName(byte[] encProtocolName, ByteBuf in, ConnectPacket connectPacket)
			throws UnsupportedEncodingException {
		String protocolName = new String(encProtocolName, "UTF-8");
		/*if (!protocolName.equalsIgnoreCase("MQIsdp")) {
			in.resetReaderIndex();//go to the first index position
			throw new CorruptedFrameException("Invalid protocol name:" + protocolName);
		}*/
		connectPacket.setProtocolName(protocolName);
	}
}
