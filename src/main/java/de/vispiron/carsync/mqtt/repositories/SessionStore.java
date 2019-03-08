package de.vispiron.carsync.mqtt.repositories;

import de.vispiron.carsync.mqtt.models.ClientSession;
import de.vispiron.carsync.mqtt.models.SessionStatus;
import de.vispiron.carsync.mqtt.models.Subscription;

/**
 *  Defines the structure SPI (Serial Peripheral Interface) as a Storage service that handle session of packets
 * @author Mehrdad A.Karami at 3/5/19
 **/
public interface SessionStore {

	void initStore();

	void updateStatus(String clientId, SessionStatus sessionStatus);

	void addNewSubscription(Subscription subscription);

	ClientSession findSessionForClient(String clientId);

	ClientSession createNewSession(String clientId, boolean cleanSession);

	void wipeSubscriptions(String clientId);
}
