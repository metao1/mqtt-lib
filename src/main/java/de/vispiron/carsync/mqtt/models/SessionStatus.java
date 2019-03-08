package de.vispiron.carsync.mqtt.models;

/**
 * Storing the state of a session
 * @author Mehrdad A.Karami at 3/5/19
 **/
public class SessionStatus {

	public  boolean cleanSession;

	public boolean active;

	SessionStatus(boolean cleanSession){
		this.cleanSession = cleanSession;
	}

	public boolean isCleanSession() {
		return cleanSession;
	}

	public boolean isActive() {
		return active;
	}

	public void setCleanSession(boolean cleanSession) {
		this.cleanSession = cleanSession;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
