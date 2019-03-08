package de.vispiron.carsync.mqtt.repositories;

import de.vispiron.carsync.mqtt.models.ClientSession;
import de.vispiron.carsync.mqtt.models.SessionStatus;
import de.vispiron.carsync.mqtt.models.Subscription;
import org.springframework.stereotype.Component;

/**
 * @author Mehrdad A.Karami at 3/7/19
 **/
@Component
public class SessionStoreImpl implements SessionStore {

	@Override
	public void initStore() {

	}

	@Override
	public void updateStatus(String clientId, SessionStatus sessionStatus) {

	}

	@Override
	public void addNewSubscription(Subscription subscription) {

	}

	@Override
	public ClientSession findSessionForClient(String clientId) {
		return null;
	}

	@Override
	public ClientSession createNewSession(String clientId, boolean cleanSession) {
		return null;
	}

	@Override
	public void wipeSubscriptions(String clientId) {

	}
}
