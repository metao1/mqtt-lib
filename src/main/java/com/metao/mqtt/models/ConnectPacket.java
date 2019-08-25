package com.metao.mqtt.models;

/**
 * @author Mehrdad A.Karami at 2/25/19
 **/

public class ConnectPacket extends PacketTypeMessage {
	private String protocolName;
	private byte protocolVersion;
	public String clientId;

	protected boolean willRetain;

	protected boolean cleanSession;

	protected boolean willFlag;
	protected byte willQos;//
	protected boolean passwordFlag;
	protected boolean userFlag;
	protected int keepAlive;

	private boolean sessionPresent;

	private int packetLength;

	private boolean willRetainFlag;
	private String willTopic;
	private String username;
	private byte[] password;
	private byte[] willMessage;

	public ConnectPacket() {
		setMessageType(CONNECT);
	}

	public String getWillTopic() {
		return willTopic;
	}

	public void setPacketLength(int packetLength) {
		this.packetLength = packetLength;
	}

	public void setCleanSession(boolean cleanSession) {
		this.cleanSession = cleanSession;
	}

	public boolean isCleanSession() {
		return cleanSession;
	}


	public int getPacketLength() {
		return packetLength;
	}

	public int getKeepAlive() {
		return keepAlive;
	}

	public byte getWillQos() {
		return willQos;
	}


	public void setKeepAlive(int keepAlive) {
		this.keepAlive = keepAlive;
	}

	public void setPasswordFlag(boolean passwordFlag) {
		this.passwordFlag = passwordFlag;
	}

	public String getProtocolName() {
		return protocolName;
	}

	public void setProtocolName(String protocolName) {
		this.protocolName = protocolName;
	}

	public void setProtocolVersion(byte protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

	public byte getProtocolVersion() {
		return protocolVersion;
	}

	public void setWillFlag(boolean willFlag) {
		this.willFlag = willFlag;
	}

	public void setUserFlag(boolean userFlag) {
		this.userFlag = userFlag;
	}

	public void setWillRetainFlag(boolean willRetainFlag) {
		this.willRetainFlag = willRetainFlag;
	}

	public void setWillQos(byte willQos) {
		this.willQos = willQos;
	}

	public void setWillTopic(String willTopic) {
		this.willTopic = willTopic;
	}

	public void setUserName(String userName) {
		this.username = userName;
	}

	public void setPassword(byte[] password) {
		this.password = password;
	}

	public boolean getCleanSession() {
		return cleanSession;
	}

	public boolean isWillFlag() {
		return willFlag;
	}

	public byte[] getWillMessage() {
		return willMessage;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public boolean isWillRetain() {
		return willRetain;
	}

    public void setWillRetain(boolean willRetain) {
        this.willRetain = willRetain;
    }

    public boolean isPasswordFlag() {
        return passwordFlag;
    }

    public boolean isUserFlag() {
        return userFlag;
    }

    public boolean isSessionPresent() {
        return sessionPresent;
    }

    public void setSessionPresent(boolean sessionPresent) {
        this.sessionPresent = sessionPresent;
    }

    public boolean isWillRetainFlag() {
        return willRetainFlag;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public byte[] getPassword() {
        return password;
    }

    public void setWillMessage(byte[] willMessage) {
        this.willMessage = willMessage;
    }

    public String getClientID() {
        return clientId;
    }

    public void setClientID(String clientID) {
        this.clientId = clientID;
    }
}
