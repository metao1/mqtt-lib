package com.metao.mqtt.repositories;

import com.metao.mqtt.models.*;
import com.metao.mqtt.models.*;

import java.util.Collection;
import java.util.List;

/**
 *  Defines the structure SPI (Serial Peripheral Interface) as a Storage service that handle session of packets
 * @author Mehrdad A.Karami at 3/5/19
 **/
public interface SessionsStore {

    void initStore();

    void updateStatus(String clientId, SessionStatus sessionStatus);

    /**
     * Add a new subscription to the session
     */
    void addNewSubscription(Subscription newSubscription);

    /**
     * Removed a specific subscription
     */
    void removeSubscription(String topic, String clientId);

    /**
     * Remove all the subscriptions of the session
     */
    void wipeSubscriptions(String sessionId);

    /**
     * Return all topic filters to recreate the subscription tree.
     */
    List<ClientTopicCouple> listAllSubscriptions();

    /**
     * @return the subscription stored by clientId and topicFilter, if any else null;
     */
    Subscription getSubscription(ClientTopicCouple couple);

    /**
     * @return true if there are subscriptions persisted with clientId
     */
    boolean contains(String clientId);

    ClientSession createNewSession(String clientId, boolean cleanSession);

    /**
     * @param clientId the client owning the session.
     * @return the session for the given clientId, null if not found.
     */
    ClientSession sessionForClient(String clientId);

    void inFlightAck(String clientId, int packetId);

    /**
     * Save the binding packetId, clientId <-> guid
     */
    void inFlight(String clientId, int packetId, String guid);

    /**
     * Return the next valid packetIdentifier for the given client session.
     */
    int nextPacketId(String clientId);

    /**
     * Store the guid to be later published.
     */
    void bindToDeliver(String guid, String clientId);

    /**
     * List the guids for retained packets for the session
     */
    Collection<String> enqueued(String clientId);

    /**
     * Remove form the queue of stored packets for session.
     */
    void removeEnqueued(String clientId, String guid);

    void pubrelWaiting(String clientId, int packetId);

    /**
     * @return the guid of message just acked.
     */
    String pubrelAcknowledged(String clientId, int packetId);

    String mapToGuid(String clientId, int packetId);

    Message getInflightMessage(String clientId, int packetId);
}
