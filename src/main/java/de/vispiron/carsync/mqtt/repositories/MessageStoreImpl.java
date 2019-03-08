package de.vispiron.carsync.mqtt.repositories;

import de.vispiron.carsync.mqtt.models.MatchingCondition;
import de.vispiron.carsync.mqtt.models.Message;

import java.util.Collection;
import java.util.List;

/**
 * @author Mehrdad A.Karami at 3/7/19
 **/

public class MessageStoreImpl implements MessageStore {

	@Override
	public void initStore() {

	}

	@Override
	public void storeRetaied(String topic, Message message) {

	}

	@Override
	public Collection<Message> searchMatching(MatchingCondition condition) {
		return null;
	}

	@Override
	public Collection<String> enqueuedMessages(String clientId) {
		return null;
	}

	@Override
	public List<Message> listMessagesInSession(String clientId, Collection<String> guids) {
		return null;
	}

	@Override
	public void dropMessagesInSession(String clientId) {

	}
}
