package com.metao.mqtt.repositories;

import com.metao.mqtt.models.MatchingCondition;
import com.metao.mqtt.models.Message;

import java.util.Collection;
import java.util.List;

public interface MessagesStore {
    /**
     * Used to initialize all persistent store structures
     */
    void initStore();

    /**
     * Persist the message.
     * If the message is empty then the topic is cleaned, else it's stored.
     */
    void storeRetained(String topic, Message message);

    /**
     * Return a list of retained packets that satisfy the condition.
     */
    Collection<Message> searchMatching(MatchingCondition condition);

    /**
     * Persist the message.
     *
     * @return the unique id in the storage (msgId).
     */
    void storePublishForFuture(Message evt);

    void removeStoredMessage(Message inflightMsg);

    void cacheForExactly(Message evt);

    void removeCacheExactlyMessage(Message message);

    /**
     * Return the list of persisted publishes for the given clientId.
     * For QoS1 and QoS2 with clean session flag, this method return the list of
     * missed publish events while the client was disconnected.
     */
    List<Message> listMessagesInSession(String clientId, Collection<String> guids);

    void dropMessagesInSession(String clientID);

    Message getMessageByGuid(String clientId, String guid);

    Message getCacheMessageByGuid(String clientId, String guid);

    void cleanRetained(String topic);
}
