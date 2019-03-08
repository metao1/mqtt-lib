package de.vispiron.carsync.mqtt.repositories;

import de.vispiron.carsync.mqtt.models.MatchingCondition;
import de.vispiron.carsync.mqtt.models.Message;

import java.util.Collection;
import java.util.List;

/**
 * Defines the structure SPI (Serial Peripheral Interface) as a Storage service that handle stores of packets
 * @author Mehrdad A.Karami at 3/5/19
 **/

public interface MessageStore {

	/**
	 * Init storage structure
	 */
	void initStore();

	/**
	 * Store the message method
	 * @param topic
	 * @param message
	 */
	void storeRetaied(String topic, Message message);

	/**
	 * Return a list of retained packets that satisfy the condition
	 * @param condition
	 * @return
	 */
	Collection<Message> searchMatching(MatchingCondition condition);

	Collection<String> enqueuedMessages(String clientId);

	List<Message> listMessagesInSession(String clientId, Collection<String> guids);

	void dropMessagesInSession(String clientId);
}
