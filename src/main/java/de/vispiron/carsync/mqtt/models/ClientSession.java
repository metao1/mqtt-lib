package de.vispiron.carsync.mqtt.models;

import de.vispiron.carsync.mqtt.repositories.MessageStore;
import de.vispiron.carsync.mqtt.repositories.SessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * A model as a Session for pack described on page 25 of MQTT 3.1.1 specification
 * * <ul>
 * * <li>The existence of a Session, even if the rest of the Session state is empty.</li>
 * * <li>The Client’s subscriptions.</li>
 * * <li>QoS 1 and QoS 2 packets which have been sent to the Client, but have not been
 * * completely acknowledged.</li>
 * * <li>QoS 1 and QoS 2 packets pending transmission to the Client.</li>
 * * <li>QoS 2 packets which have been received from the Client, but have not been
 * * completely acknowledged.</li>
 * * <li>Optionally, QoS 0 packets pending transmission to the Client.</li>
 * * </ul>
 *
 * @author Mehrdad A.Karami at 3/5/19
 **/

public class ClientSession {
	private final static Logger log = LoggerFactory.getLogger(ClientSession.class);

	private final String clientId;

	private final MessageStore messageStore;

	private final SessionStore sessionStore;

	private LinkedBlockingDeque<PacketTypeMessage> queueOfMessages = new LinkedBlockingDeque<>(1024);
	private SessionStatus sessionStatus;

	public ClientSession(String clientId, MessageStore messageStore, SessionStore sessionStore) {
		this.clientId = clientId;
		this.messageStore = messageStore;
		this.sessionStore = sessionStore;
	}

	/**
	 * @return the list of all messages to be delivered
	 */
	public List<Message> getStoredMessges() {
		Collection<String> guids = this.messageStore.enqueuedMessages(this.clientId);
		return messageStore.listMessagesInSession(clientId, guids);
	}

	public boolean isActive() {
		return this.sessionStatus.isActive();
	}

	public void cleanSession(boolean cleanSession) {
		this.sessionStatus.setCleanSession(cleanSession);
		this.sessionStore.updateStatus(this.clientId, this.sessionStatus);
	}

	public void setActive(boolean status) {
		this.sessionStatus.setActive(status);
		this.sessionStore.updateStatus(this.clientId, this.sessionStatus);
	}

	public void cleanSession() {
		log.info("Cleaning old saved subscriptions for pack <{}>", this.clientId);
		sessionStore.wipeSubscriptions(this.clientId);
		messageStore.dropMessagesInSession(this.clientId);
	}

	public void disconnect() {
		if(this.sessionStatus.isCleanSession()){
			cleanSession();
		}
		setActive(false);
	}

	public String getClientId() {
		return clientId;
	}

	public boolean subscribe(Subscription newSubscription) {
		//todo impl the mechanism and subscription and topic parsing
		return false;
	}
}
