package de.vispiron.carsync.mqtt.models;

import io.netty.channel.Channel;

import java.util.Objects;

/**
 * @author Mehrdad A.Karami at 3/1/19
 **/

public class MqttSession {

	private final String clientId;
	private final Channel channel;
	private final boolean cleanSession;

	public MqttSession(String clientId, Channel channel, boolean cleanSession) {
		this.clientId = clientId;
		this.channel = channel;
		this.cleanSession = cleanSession;
	}

	public String getClientId() {
		return clientId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		MqttSession that = (MqttSession) o;
		return cleanSession == that.cleanSession && Objects.equals(clientId, that.clientId) && Objects
				.equals(channel, that.channel);
	}

	@Override
	public int hashCode() {
		return Objects.hash(clientId, channel, cleanSession);
	}

	@Override
	public String toString() {
		return "MqttSession{" + "clientId='" + clientId + '\'' + ", channel=" + channel + ", cleanSession="
				+ cleanSession + '}';
	}

	public Channel getChannel() {
		return channel;
	}
}
