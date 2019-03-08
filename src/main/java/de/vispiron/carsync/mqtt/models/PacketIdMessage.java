package de.vispiron.carsync.mqtt.models;

/**
 * @author Mehrdad A.Karami at 3/7/19
 **/

public class PacketIdMessage extends PacketTypeMessage {

	private Integer packetId;// Could be null iof the Qos is zero!

	public Integer getPacketId() {
		return null;
	}

	public void setPacketId(Integer packetId) {
		this.packetId = packetId;
	}
}
