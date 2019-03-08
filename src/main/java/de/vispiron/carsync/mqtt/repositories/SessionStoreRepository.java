package de.vispiron.carsync.mqtt.repositories;

/**
 * @author Mehrdad A.Karami at 3/7/19
 **/

public class SessionStoreRepository {

	public SessionStoreRepository(){
	}

	public SessionStore getSessionStore() {
		SessionStore sessionStore = new SessionStoreImpl();
		sessionStore.initStore();
		return sessionStore;
	}

	public MessageStore getMessageStore(){
		MessageStore messageStore = new MessageStoreImpl();
		messageStore.initStore();
		return messageStore;
	}

	public void close(){
		//todo close the database
	}

}
