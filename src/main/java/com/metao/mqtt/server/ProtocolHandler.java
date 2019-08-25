package com.metao.mqtt.server;

import com.metao.mqtt.models.*;
import com.metao.mqtt.repositories.MessagesStore;
import com.metao.mqtt.repositories.SessionsStore;
import com.metao.mqtt.repositories.SubscriptionsStore;
import com.metao.mqtt.utils.Utils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import static com.metao.mqtt.utils.Utils.*;

/**
 * @author Mehrdad A.Karami at 2/25/19
 **/

@Component
public class ProtocolHandler {

    private static Logger LOG = LoggerFactory.getLogger(ProtocolHandler.class.getName());

    private ConcurrentMap<String, MqttSession> mqttClients = new ConcurrentHashMap<>();

    private SubscriptionsStore subscriptions;

    @Autowired
    SessionsStore sessionsStore;

    @Autowired
    private MessagesStore messageStore;

    @Autowired
    private Authenticator authenticator;

    @Autowired
    private BrokerInterceptor interceptor;

    private ConcurrentMap<String, WillMessage> willStore = new ConcurrentHashMap<>();

    @Autowired
    private Authorizator authorizator;

    @Autowired
    private MqttProperties mqttProperties;


    @PostConstruct
    void init() {
        subscriptions = new SubscriptionsStore();
        subscriptions.init(sessionsStore);
    }

    /**
     * Accept packet procedure and send it it the server
     *
     * @param channel channel to accept
     * @param packet  packet to accept
     */
    public void processConnect(Channel channel, ConnectPacket packet) {

        LOG.debug("CONNECT for client <{}>", packet.getClientId());
        if (packet.getProtocolVersion() != VERSION_3_1 && packet.getProtocolVersion() != VERSION_3_1_1) {
            ConnAckPacket badProto = new ConnAckPacket();
            badProto.setReturnCode(ConnAckPacket.UNNACEPTABLE_PROTOCOL_VERSION);
            LOG.warn("processConnect sent bad proto ConnAck");
            channel.writeAndFlush(badProto);
            channel.close();
            return;
        }

        if (packet.getClientId() == null || packet.getClientId().length() == 0) {
            if (!packet.isCleanSession() || !mqttProperties.getAllowZeroByteClientId()) {
                ConnAckPacket okResp = new ConnAckPacket();
                okResp.setReturnCode(ConnAckPacket.IDENTIFIER_REJECTED);
                channel.writeAndFlush(okResp);
                channel.close();
                return;
            }

            // Generating client id.
            String randomIdentifier = UUID.randomUUID().toString().replace("-", "");
            packet.setClientID(randomIdentifier);
            LOG.info("Client connected with server generated identifier: {}", randomIdentifier);
        }

        //handle user authentication
        if (packet.isUserFlag()) {
            byte[] pwd = null;
            if (packet.isPasswordFlag()) {
                pwd = packet.getPassword();
            } else if (!mqttProperties.getAllowAnonymous()) {
                failedCredentials(channel);
                return;
            }
            if (!authenticator.checkValid(packet.getClientId(), packet.getUsername(), pwd, mqttProperties.getAllowZeroByteClientId())) {
                failedCredentials(channel);
                channel.close();
                return;
            }
            Utils.userName(channel, packet.getUsername());
        } else if (!mqttProperties.getAllowAnonymous()) {
            failedCredentials(channel);
            return;
        }

        //if an old client with the same ID already exists close its session.
        if (mqttClients.containsKey(packet.getClientId())) {
            LOG.info("Found an existing connection with same client ID <{}>, forcing to close", packet.getClientId());
            //clean the subscriptions if the old used a cleanSession = true
            Channel oldChannel = mqttClients.get(packet.getClientId()).getChannel();
            ClientSession oldClientSession = sessionsStore.sessionForClient(packet.getClientId());
            oldClientSession.disconnect();
            Utils.sessionStolen(oldChannel, true);
            oldChannel.close();
            LOG.debug("Existing connection with same client ID <{}>, forced to close", packet.getClientId());
        }

        MqttSession connDescr = new MqttSession(packet.getClientId(), channel, packet.isCleanSession());
        mqttClients.put(packet.getClientId(), connDescr);

        int keepAlive = packet.getKeepAlive();
        LOG.debug("Connect with keepAlive {} s", keepAlive);
        Utils.keepAlive(channel, keepAlive);
        //session.attr(Utils.ATTR_KEY_CLEANSESSION).set(msg.isCleanSession());
        Utils.cleanSession(channel, packet.isCleanSession());
        //used to track the client in the subscription and publishing phases.
        //session.attr(Utils.ATTR_KEY_CLIENTID).set(msg.getClientId());
        Utils.clientId(channel, packet.getClientId());
        LOG.debug("Connect create session <{}>", channel);

        setIdleTime(channel.pipeline(), Math.round(keepAlive * 1.5f));

        //Handle will flag
        if (packet.isWillFlag()) {
            QosType willQos = QosType.valueOf(packet.getWillQos());
            byte[] willPayload = packet.getWillMessage();
            ByteBuffer bb = (ByteBuffer) ByteBuffer.allocate(willPayload.length).put(willPayload).flip();
            //save the will testament in the clientId store
            WillMessage will = new WillMessage(packet.getWillTopic(), bb, packet.isWillRetain(), willQos);
            willStore.put(packet.getClientId(), will);
            LOG.info("Session for clientId <{}> with will to topic {}", packet.getClientId(), packet.getWillTopic());
        }

        ConnAckPacket okResp = new ConnAckPacket();
        okResp.setReturnCode(ConnAckPacket.CONNECTION_ACCEPTED);

        ClientSession clientSession = sessionsStore.sessionForClient(packet.getClientId());
        boolean isSessionAlreadyStored = clientSession != null;
        if (!packet.isCleanSession() && isSessionAlreadyStored) {
            okResp.setSessionPresent(true);
        }
        if (isSessionAlreadyStored) {
            clientSession.cleanSession(packet.isCleanSession());
        }
        channel.writeAndFlush(okResp);
        interceptor.notifyClientConnected(packet);

        if (!isSessionAlreadyStored) {
            LOG.info("Create persistent session for clientId <{}>", packet.getClientId());
            clientSession = sessionsStore.createNewSession(packet.getClientId(), packet.isCleanSession());
        }
        clientSession.activate();
        if (packet.isCleanSession()) {
            clientSession.cleanSession();
        }
        LOG.info("Connected client ID <{}> with clean session {}", packet.getClientId(), packet.isCleanSession());
        if (!packet.isCleanSession()) {
            //force the republish of stored QoS1 and QoS2
            republishStoredInSession(clientSession);
        }
        int flushIntervalMs = 500/*(keepAlive * 1000) / 2*/;
        setupAutoFlusher(channel.pipeline(), flushIntervalMs);
        LOG.info("CONNECT processed");
    }

    private void failedCredentials(Channel session) {
        ConnAckPacket okResp = new ConnAckPacket();
        okResp.setReturnCode(ConnAckPacket.BAD_USERNAME_OR_PASSWORD);
        session.writeAndFlush(okResp);
        session.close();
        LOG.info("Client {} failed to connect with bad username or password.", session);
    }

    private void setupAutoFlusher(ChannelPipeline pipeline, int flushInterval) {
        AutoFlusherHandler autoFlusherHandler = new AutoFlusherHandler(flushInterval, TimeUnit.MICROSECONDS);
        try {
            pipeline.addAfter("idleEventHandler", "autoFlusher", autoFlusherHandler);
        } catch (NoSuchElementException e) {
            pipeline.addFirst("autoFlusher", autoFlusherHandler);
        }
    }

    /**
     * Republish Qos1 & Qos2 packets stored into the session for the clientId
     *
     * @param clientSession
     */
    private void republishStoredInSession(ClientSession clientSession) {
        //todo impl mechanism for the republish of the messages
    }

    private void handleWillFlag(ConnectPacket packet) {
        if (packet.isWillFlag()) {
            QosType willQos = QosType.valueOf(packet.getWillQos());
            byte[] willPayload = packet.getWillMessage();
            //ByteBuffer byteBuffer = ByteBuffer.allocate(willPayload.length).put(willPayload);
            ByteBuffer byteBuffer = (ByteBuffer) ByteBuffer.allocate(willPayload.length).put(willPayload).flip();
            //save the will in the clientId store
            WillMessage willMessage = new WillMessage(packet.getWillTopic(), byteBuffer, packet.isWillRetain(),
                willQos);
            willStore.put(packet.getClientId(), willMessage);
            LOG.info("Session for pack <{}> with will to topic {}", packet.getClientId(), packet.getWillTopic());
        }
    }

    private void manageTheSession(Channel channel, ConnectPacket packet) {
        //Keep the channel alive for mqtt Clients
        LOG.info("Keep channel alive with {} ", channel);
        channel.attr(ATTR_KEY_KEEPALIVE).set(packet.getKeepAlive());

        //Clean the session
        channel.attr(ATTR_KEY_CLEANSESSION).set(packet.getCleanSession());

        //Assign the pack an ID (later we need it for disconnection message)
        channel.attr(ATTR_KEY_CLIENTID).set(packet.getClientId());

        MqttSession mqttSession = new MqttSession(packet.getClientId(), channel, packet.isCleanSession());
        mqttClients.put(packet.getClientId(), mqttSession);//saving the session for packets with the same user ids
        LOG.info("Session {} created ", channel);
        //set the idle time
        setIdleTime(channel.pipeline(), Math.round(packet.getKeepAlive() * 1.5f));
    }

    private void setIdleTime(ChannelPipeline pipeline, int idleTime) {
        if (pipeline.names().contains("idleStateHandler")) {
            pipeline.remove("idleStateHandler");
        }
        pipeline.addFirst("idleStateHandler", new IdleStateHandler(0, 0, idleTime));
    }

    /**
     * Disconnect from the channel and close the session
     *
     * @param channel
     */
    public void processDisconnect(@NonNull Channel channel) {
        channel.flush();
        String clientID = Utils.clientId(channel);
        boolean cleanSession = Utils.cleanSession(channel);
        LOG.info("DISCONNECT client <{}> with clean session {}", clientID, cleanSession);
        ClientSession clientSession = sessionsStore.sessionForClient(clientID);
        clientSession.disconnect();

        mqttClients.remove(clientID);
        channel.close();

        //cleanup the will store
        willStore.remove(clientID);

        String username = Utils.userName(channel);
        interceptor.notifyClientDisconnected(clientID, username);
        LOG.info("DISCONNECT client <{}> finished", clientID, cleanSession);
    }

    public void processSubscribe(Channel channel, SubscribePacket msg) {
        String clientId = Utils.clientId(channel);
        LOG.debug("SUBSCRIBE client <{}> packetID {}", clientId, msg.getPacketId());

        ClientSession clientSession = sessionsStore.sessionForClient(clientId);
        verifyToActivate(clientSession);
        //ack the client
        SubscribeAckPacket ackMessage = new SubscribeAckPacket();
        ackMessage.setPacketId(msg.getPacketId());

        String username = Utils.userName(channel);
        List<Subscription> newSubscriptions = new ArrayList<>();
        for (SubscribePacket.DeCouple req : msg.getSubscriptions()) {
            if (!authorizator.canRead(req.getTopicFilter(), username, clientSession.clientId)) {
                //send SUBACK with 0x80, the user hasn't credentials to read the topic
                LOG.debug("topic {} doesn't have read credentials", req.getTopicFilter());
                ackMessage.addType(QosType.FAILURE);
                continue;
            }

            QosType qos = QosType.valueOf(req.getQos());
            Subscription newSubscription = new Subscription(clientId, req.getTopicFilter(), qos);
            boolean valid = clientSession.subscribe(newSubscription);
            ackMessage.addType(valid ? qos : QosType.FAILURE);
            if (valid) {
                newSubscriptions.add(newSubscription);
            }
        }

        //save session, persist subscriptions from session
        LOG.debug("SUBACK for packetID {}", msg.getPacketId());
        if (LOG.isTraceEnabled()) {
            LOG.trace("subscription tree {}", subscriptions.dumpTree());
        }

        for (Subscription subscription : newSubscriptions) {
            subscribeSingleTopic(subscription);
        }
        channel.writeAndFlush(ackMessage);

        //fire the persisted packets in session
        for (Subscription subscription : newSubscriptions) {
            publishStoredMessagesInSession(subscription, username);
        }
    }

    private void subscribeSingleTopic(final Subscription newSubscription) {
        LOG.debug("Subscribing {}", newSubscription);
        subscriptions.add(newSubscription.asClientTopicCouple());
    }

    void processUnsubscribe(Channel channel, UnsubscribeAckPacket msg) {
        List<String> topics = msg.topicFilters();
        int messageID = msg.getPacketId();
        String clientID = Utils.clientId(channel);

        LOG.debug("UNSUBSCRIBE subscription on topics {} for clientId <{}>", topics, clientID);

        ClientSession clientSession = sessionsStore.sessionForClient(clientID);
        verifyToActivate(clientSession);
        for (String topic : topics) {
            boolean validTopic = SubscriptionsStore.validate(topic);
            if (!validTopic) {
                //close the connection, not valid topicFilter is a protocol violation
                channel.close();
                LOG.warn("UNSUBSCRIBE found an invalid topic filter <{}> for clientId <{}>", topic, clientID);
                return;
            }

            subscriptions.removeSubscription(topic, clientID);
            clientSession.unsubscribe(topic);
            String username = Utils.userName(channel);
            interceptor.notifyTopicUnsubscribed(topic, clientID, username);
        }

        //ack the client
        UnsubscribeAckPacket ackMessage = new UnsubscribeAckPacket();
        ackMessage.setPacketId(messageID);

        LOG.info("replying with UnsubAck to MSG ID {}", messageID);
        channel.writeAndFlush(ackMessage);
    }

    private void activeTheSession(ClientSession clientSession) {
        if (clientSession == null) {
            LOG.warn("no session specified!");
            return;
        }
        String clientId = clientSession.getClientId();
        if (mqttClients.containsKey(clientId)) {
            clientSession.setActive(true);
        }
    }

    private void verifyToActivate(ClientSession targetSession) {
        if (targetSession == null) {
            return;
        }
        String clientId = targetSession.clientId;
        if (mqttClients.containsKey(clientId)) {
            targetSession.activate();
        }
    }

    public void processPublishAck(Channel channel, PublishAckPacket message) {
        String clientId = Utils.clientId(channel);

        if (clientId == null) {
            channel.close();
            return;
        }

        int packetId = message.getPacketId();
        String username = Utils.userName(channel);
        LOG.trace("retrieving inflight for packetId <{}>", packetId);

        //Remove the message from message store
        ClientSession targetSession = sessionsStore.sessionForClient(clientId);
        verifyToActivate(targetSession);
        Message inflightMsg = targetSession.getInflightMessage(packetId);
        targetSession.inFlightAcknowledged(packetId);

        String topic = inflightMsg.getTopic();

        interceptor.notifyMessageAcknowledged(new InterceptAcknowledgedMessage(inflightMsg, topic, username));
    }

    public void processPublish(Channel channel, PublishPacket message) {
        LOG.trace("PUB --PUBLISH--> SRV executePublish invoked with {}", message);
        String clientId = Utils.clientId(channel);

        if (clientId == null) {
            channel.close();
            return;
        }

        final String topic = message.getTopicName();
        //check if the topic can be wrote
        String username = Utils.userName(channel);
        if (!authorizator.canWrite(topic, username, clientId)) {
            LOG.debug("topic {} doesn't have write credentials", topic);
            return;
        }
        final QosType qos = message.getQos();
        final Integer packetId = message.getPacketId();
        LOG.info("PUBLISH from clientId <{}> on topic <{}> with QoS {}", clientId, topic, qos);

        Message toStoreMsg = asStoredMessage(message);
        toStoreMsg.setClientId(clientId);
        if (qos == QosType.MOST_ONE) { //QoS0
            route2Subscribers(toStoreMsg);
        } else if (qos == QosType.LEAST_ONE) { //QoS1
            route2Subscribers(toStoreMsg);
            if (message.isLocal()) {
                sendPubAck(clientId, packetId);
            }
            LOG.info("replying with PubAck to MSG ID {}", packetId);
        } else if (qos == QosType.EXACTLY_ONCE) { //QoS2
            messageStore.cacheForExactly(toStoreMsg);

            if (message.isLocal()) {
                sendPubRec(clientId, packetId);
            }
            //Next the client will send us a pub rel
            //NB publish to subscribers for QoS 2 happen upon PUBREL from publisher
        }

        if (message.isRetainFlag()) {
            if (qos == QosType.MOST_ONE) {
                //QoS == 0 && retain => clean old retained
                messageStore.cleanRetained(topic);
            } else {
                if (!message.getPayload().hasRemaining()) {
                    messageStore.cleanRetained(topic);
                } else {
                    messageStore.storeRetained(topic, toStoreMsg);
                }
            }
        }
        interceptor.notifyTopicPublished(message, clientId, username);
    }

    private void route2Subscribers(Message toStoreMsg) {
        final String topic = toStoreMsg.getTopic();
        final QosType publishingQos = toStoreMsg.getQos();
        final ByteBuffer origMessage = toStoreMsg.getMessage();
        LOG.debug("route2Subscribers republishing to existing subscribers that matches the topic {}", topic);
        if (LOG.isTraceEnabled()) {
            LOG.trace("content <{}>", Utils.payload2Str(origMessage));
            LOG.trace("subscription tree {}", subscriptions.dumpTree());
        }

        List<Subscription> topicMatchingSubscriptions = subscriptions.matches(topic);
        LOG.trace("Found {} matching subscriptions to <{}>", topicMatchingSubscriptions.size(), topic);
        for (final Subscription sub : topicMatchingSubscriptions) {
            QosType qos = publishingQos;
            if (qos.byteValue() > sub.getRequestedQos().byteValue()) {
                qos = sub.getRequestedQos();
            }
            ClientSession targetSession = sessionsStore.sessionForClient(sub.getClientId());
            verifyToActivate(targetSession);

            LOG.debug("Broker republishing to client <{}> topic <{}> qos <{}>, active {}",
                sub.getClientId(), sub.getTopicFilter(), qos, targetSession.isActive());
            ByteBuffer message = origMessage.duplicate();
            if (qos == QosType.MOST_ONE && targetSession.isActive() && mqttClients.containsKey(targetSession.clientId)) {
                //QoS 0
                directSend(targetSession, topic, qos, message, false, null);
            } else {
                Message storedMessage = new Message();
                storedMessage.setMsgId(toStoreMsg.getMsgId());
                storedMessage.setPacketId(toStoreMsg.getPacketId());
                storedMessage.setPayload(toStoreMsg.getPayload());
                storedMessage.setQos(toStoreMsg.getQos());
                storedMessage.setRetained(toStoreMsg.isRetained());
                storedMessage.setTopic(toStoreMsg.getTopic());

                storedMessage.setClientId(sub.getClientId());

                messageStore.storePublishForFuture(storedMessage);
                //QoS 1 or 2
                //if the target subscription is not clean session and is not connected => store it
                if (!targetSession.isCleanSession() && !targetSession.isActive()) {
                    //store the message in targetSession queue to deliver
                    targetSession.enqueueToDeliver(toStoreMsg.getMsgId());
                } else {
                    //publish
                    if (targetSession.isActive() && mqttClients.containsKey(targetSession.clientId)) {
                        int packetId = targetSession.nextPacketId();
                        targetSession.inFlightAckWaiting(toStoreMsg.getMsgId(), packetId);
                        directSend(targetSession, topic, qos, message, false, packetId);
                    }
                }
            }
        }
    }

    public void processPingRequest(Channel channel) {
        PingResponsePacket pingResp = new PingResponsePacket();
        channel.writeAndFlush(pingResp);
    }

    public void processPublishReceived(Channel channel, PublishReceivePacket message) {
        String clientID = Utils.clientId(channel);

        if (clientID == null) {
            channel.close();
            return;
        }

        ClientSession targetSession = sessionsStore.sessionForClient(clientID);
        verifyToActivate(targetSession);
        //remove from the inflight and move to the QoS2 second phase queue
        int packetId = message.getPacketId();
        targetSession.pubrelWaiting(packetId);
        //once received a PUBREC reply with a PUBREL(packetId)
        LOG.debug("\t\tSRV <--PUBREC-- SUB processPubRec invoked for clientId {} ad packetId {}", clientID, packetId);
        PubRelPacket pubRelMessage = new PubRelPacket();
        pubRelMessage.setPacketId(packetId);

        channel.writeAndFlush(pubRelMessage);
    }

    private static Message asStoredMessage(PublishPacket msg) {
        Message stored = new Message(msg.getPayload().array(), msg.getQos(), msg.getTopicName());
        stored.setRetained(msg.isRetainFlag());
        stored.setPacketId(msg.getPacketId());
        stored.setMsgId(UUID.randomUUID().toString());
        return stored;
    }

    private static Message asStoredMessage(WillMessage will) {
        Message pub = new Message(will.getPayload().array(), will.getQosType(), will.getTopic());
        pub.setRetained(will.isRetained());
        return pub;
    }

    private void publishStoredMessagesInSession(final Subscription newSubscription, String username) {
        LOG.debug("Publish persisted packets in session {}", newSubscription);

        //scans retained packets to be published to the new subscription
        //TODO this is ugly, it does a linear scan on potential big dataset
        Collection<Message> messages = messageStore.searchMatching(new MatchingCondition() {
            @Override
            public boolean match(String key) {
                return SubscriptionsStore.matchTopics(key, newSubscription.getTopicFilter());
            }
        });

        LOG.debug("Found {} packets to republish", messages.size());
        ClientSession targetSession = sessionsStore.sessionForClient(newSubscription.getClientId());
        verifyToActivate(targetSession);
        for (Message storedMsg : messages) {
            //fire as retained the message
            LOG.trace("send publish message for topic {}", newSubscription.getTopicFilter());
            Integer packetId = storedMsg.getQos() == QosType.MOST_ONE ? null : targetSession.nextPacketId();
            if (packetId != null) {
                LOG.trace("Adding to inflight <{}>", packetId);
                targetSession.inFlightAckWaiting(storedMsg.getMsgId(), packetId);
            }
            directSend(targetSession, storedMsg.getTopic(), storedMsg.getQos(), storedMsg.getPayloadBuffer(), true, packetId);
        }

        //notify the Observables
        interceptor.notifyTopicSubscribed(newSubscription, username);
    }

    protected void directSend(ClientSession clientsession, String topic, QosType qos,
                              ByteBuffer message, boolean retained, Integer packetId) {
        String clientId = clientsession.clientId;
        LOG.debug("directSend invoked clientId <{}> on topic <{}> QoS {} retained {} packetId {}",
            clientId, topic, qos, retained, packetId);
        PublishPacket pubPacket = new PublishPacket();
        pubPacket.setRetainFlag(retained);
        pubPacket.setTopicName(topic);
        pubPacket.setQosType(qos);
        pubPacket.setPayload(message);

        LOG.info("send publish message to <{}> on topic <{}>", clientId, topic);
        if (LOG.isDebugEnabled()) {
            LOG.debug("content <{}>", Utils.payload2Str(message));
        }
        //set the PacketIdentifier only for QoS > 0
        if (pubPacket.getQos() != QosType.MOST_ONE) {
            pubPacket.setPacketId(packetId);
        } else {
            if (packetId != null) {
                throw new RuntimeException("Internal bad error, trying to forwardPublish a QoS 0 message " +
                    "with PacketIdentifier: " + packetId);
            }
        }

        if (mqttClients == null) {
            throw new RuntimeException("Internal bad error, found clientIds to null while it should be " +
                "initialized, somewhere it's overwritten!!");
        }
        if (mqttClients.get(clientId) == null) {
            //TODO while we were publishing to the target client, that client disconnected,
            // could happen is not an error HANDLE IT
            throw new RuntimeException(String.format("Can't find a ConnectionDescriptor for client <%s> in cache <%s>",
                clientId, mqttClients));
        }
        Channel channel = mqttClients.get(clientId).getChannel();
        LOG.trace("Session for clientId {}", clientId);
        if (channel.isWritable()) {
            //if channel is writable don't enqueue
            channel.write(pubPacket);
        } else {
            //enqueue to the client session
            clientsession.enqueue(pubPacket);
        }
    }

    private void sendPubRec(String clientId, int packetId) {
        LOG.trace("PUB <--PUBREC-- SRV sendPubRec invoked for clientId {} with packetId {}", clientId, packetId);
        PubRecPacket pubRecPacket = new PubRecPacket();
        pubRecPacket.setPacketId(packetId);
        mqttClients.get(clientId).getChannel().writeAndFlush(pubRecPacket);
    }

    private void sendPubAck(String clientId, int packetId) {
        LOG.trace("sendPubAck invoked");
        PublishAckPacket pubAckPacket = new PublishAckPacket();
        pubAckPacket.setPacketId(packetId);

        try {
            if (mqttClients == null) {
                throw new RuntimeException("Internal bad error, found clientIds to null while it should be initialized, somewhere it's overwritten!!");
            }
            LOG.debug("clientIds are {}", mqttClients);
            if (mqttClients.get(clientId) == null) {
                throw new RuntimeException(String.format("Can't find a ConnectionDescriptor for client %s in cache %s", clientId, mqttClients));
            }
            mqttClients.get(clientId).getChannel().writeAndFlush(pubAckPacket);
        } catch (Throwable t) {
            LOG.error(null, t);
        }
    }

    public void notifyChannelWritable(Channel channel) {
        String clientId = Utils.clientId(channel);
        ClientSession clientSession = sessionsStore.sessionForClient(clientId);
        boolean emptyQueue = false;
        while (channel.isWritable() && !emptyQueue) {
            PacketTypeMessage packet = clientSession.dequeue();
            if (packet == null) {
                emptyQueue = true;
            } else {
                channel.write(packet);
            }
        }
        channel.flush();
    }

    public boolean addInterceptHandler(InterceptHandler interceptHandler) {
        return this.interceptor.addInterceptHandler(interceptHandler);
    }

    public boolean removeInterceptHandler(InterceptHandler interceptHandler) {
        return this.interceptor.removeInterceptHandler(interceptHandler);
    }

    public void processPublishComplete(Channel channel, PubCompPacket message) {
        String clientId = Utils.clientId(channel);
        int packetId = message.getPacketId();
        LOG.debug("\t\tSRV <--PUBCOMP-- SUB processPubComp invoked for clientId {} ad packetId {}", clientId, packetId);
        //once received the PUBCOMP then remove the message from the temp memory
        ClientSession targetSession = sessionsStore.sessionForClient(clientId);
        verifyToActivate(targetSession);
        Message inflightMsg = targetSession.secondPhaseAcknowledged(packetId);

        targetSession.removeStoredMessage(inflightMsg);

        String username = Utils.userName(channel);
        String topic = inflightMsg.getTopic();
        interceptor.notifyMessageAcknowledged(new InterceptAcknowledgedMessage(inflightMsg, topic, username));
    }
}
