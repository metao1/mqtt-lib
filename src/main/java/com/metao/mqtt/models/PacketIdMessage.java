package com.metao.mqtt.models;

/**
 * @author Mehrdad A.Karami at 3/7/19
 **/

public class PacketIdMessage extends PacketTypeMessage {

	private Integer packetId;// Could be null if the Qos is zero!

	public Integer getPacketId() {
		return this.packetId;
	}

	public void setPacketId(Integer packetId) {
		this.packetId = packetId;
	}
}
