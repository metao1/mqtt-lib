package de.vispiron.carsync.mqtt.server;

import de.vispiron.carsync.mqtt.models.*;
import de.vispiron.carsync.mqtt.repositories.SessionStore;
import de.vispiron.carsync.mqtt.utils.Utils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import static de.vispiron.carsync.mqtt.utils.Utils.*;

/**
 * @author Mehrdad A.Karami at 2/25/19
 **/

@Component
public class ProtocolHandler {
	private static Logger log = LoggerFactory.getILoggerFactory().getLogger(ProtocolHandler.class.getName());

	private ConcurrentMap<String, MqttSession> mqttClients = new ConcurrentHashMap<>();

	@Autowired
	SessionStore sessionStore;

	private ConcurrentMap<String, WillMessage> willStore = new ConcurrentHashMap<>();

	/**
	 * Accept packet procedure and send it it the server
	 *
	 * @param channel channel to accept
	 * @param packet  packet to accept
	 */
	public void processConnect(Channel channel, ConnectPacket packet) {

		log.info("CONNECT for pack <{}>", packet.getClientId());
		if (packet.getProtocolVersion() != VERSION_3_1 && packet.getProtocolVersion() != VERSION_3_1_1) {
			ConnAckPacket badProtocolPacket = new ConnAckPacket();
			badProtocolPacket.setReturnCode(ConnAckPacket.UNACCEPTABLE_PROTOCOL_VERSION);
			log.warn("Connection was with a bad protocol version {}", packet.getProtocolVersion());
			log.error("Closing the channel...");
			channel.writeAndFlush(badProtocolPacket);
			channel.close();
			return;
		}
		if (!Utils.isValid(packet.getClientId())) {
			if (!packet.isCleanSession()) {
				log.error("Client session is not cleaned for the reconnect");
				ConnAckPacket okResponse = new ConnAckPacket();
				okResponse.setReturnCode(ConnAckPacket.IDENTIFIER_REJECTED);
				channel.writeAndFlush(okResponse);
				channel.close();
				return;
			}
			log.warn("Client has not any id, assigning and id for him");
			//otherwise generate a random id for the pack which has connected in the server
			String randomId = Utils.generateUniqueId();
			packet.setClientId(randomId);
			log.info("Client connected with server with generated id : {} ", randomId);
		}
		//if the old user is sending the same packet id
		if (mqttClients.containsKey(packet.getClientId())) {
			log.warn("Found an existing connection with the same pack ID <{}>", packet.getClientId());
			//retrieving the old session and closing the old session then close the channel
			Channel oldMqttChannel = mqttClients.get(packet.getClientId()).getChannel();
			ClientSession oldClientSession = sessionStore.findSessionForClient(packet.getClientId());
			oldClientSession.setActive(false);
			Utils.sessionStolen(oldMqttChannel, true);
			oldMqttChannel.close();
			log.info("Existing connection with the same Client ID <{}>, forced closed", packet.getClientId());
		}
		//Creating new session for the pack
		MqttSession mqttSession = new MqttSession(packet.getClientId(), channel, packet.isCleanSession());
		mqttClients.put(packet.getClientId(), mqttSession);
		//manage the pack session
		manageTheSession(channel, packet);
		//handle will flag
		handleWillFlag(packet);

		ClientSession clientSession = sessionStore.findSessionForClient(packet.getClientId());

		boolean isSessionAlreadyStored = clientSession != null;
		ConnAckPacket connectAckResponsePacket = new ConnAckPacket();
		connectAckResponsePacket.setReturnCode(ConnAckPacket.CONNECTION_ACCEPTED);
		connectAckResponsePacket.setSessionPresent(!packet.isCleanSession() && isSessionAlreadyStored);
		//send back connection accepted signal
		channel.writeAndFlush(connectAckResponsePacket);

		if (isSessionAlreadyStored) {
			clientSession.cleanSession(packet.isCleanSession());
		} else {
			clientSession = sessionStore.createNewSession(packet.getClientId(), packet.isCleanSession());
		}
		clientSession.setActive(true);
		if (packet.isCleanSession()) {
			clientSession.cleanSession();
		}
		log.info("Connected pack ID <{}> with clean session {}", packet.getClientId(), packet.isCleanSession());
		if (!packet.isCleanSession()) {
			//force to republish of store Qos1 & Qos2
			republishStoredInSession(clientSession);
		}
		int flushInterval = 500;// (keepAlive * 1000) / 2
		setupAutoFlusher(channel.pipeline(), flushInterval);
		log.info("CONNECT processed");
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
			ByteBuffer byteBuffer = ByteBuffer.allocate(willPayload.length).put(willPayload).flip();
			//save the will in the clientId store
			WillMessage willMessage = new WillMessage(packet.getWillTopic(), byteBuffer, packet.isWillRetain(),
					willQos);
			willStore.put(packet.getClientId(), willMessage);
			log.info("Session for pack <{}> with will to topic {}", packet.getClientId(), packet.getWillTopic());
		}
	}

	private void manageTheSession(Channel channel, ConnectPacket packet) {
		//Keep the channel alive for mqtt Clients
		log.info("Keep channel alive with {} ", channel);
		channel.attr(ATTR_KEY_KEEPALIVE).set(packet.getKeepAlive());

		//Clean the session
		channel.attr(ATTR_KEY_CLEANSESSION).set(packet.getCleanSession());

		//Assign the pack an ID (later we need it for disconnection message)
		channel.attr(ATTR_KEY_CLIENTID).set(packet.getClientId());

		MqttSession mqttSession = new MqttSession(packet.getClientId(), channel, packet.isCleanSession());
		mqttClients.put(packet.getClientId(), mqttSession);//saving the session for packets with the same user ids
		log.info("Session {} created ", channel);
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
	public void processDisconnect(
			@NonNull
					Channel channel) {
		channel.flush();
		String clientId = Utils.getClientId(channel);
		boolean cleanSession = Utils.cleanSession(channel);
		log.info("pack <{}> disconnected with session <{}>", clientId, cleanSession);
		ClientSession clientSession = sessionStore.findSessionForClient(clientId);
		clientSession.disconnect();
		//clean up the will store
		mqttClients.remove(clientId);
		log.info("pack <{}> disconnected? <{}>", clientId, clientSession);
	}

	public void processSubscribe(Channel channel, SubscribeMessage subscribeMessage) {
		String clientId = Utils.getClientId(channel);
		log.info("pack <{}> requests subscribed with packet id <{}>", clientId, subscribeMessage.getPacketId());
		//get the pack session
		ClientSession clientSession = sessionStore.findSessionForClient(clientId);
		activeTheSession(clientSession);
		subscribeAcknowledge(clientId, clientSession, subscribeMessage,channel);
	}

	private void subscribeAcknowledge(String clientId, ClientSession clientSession, SubscribeMessage subscribeMessage, Channel channel) {
		Queue<Subscription> newSubscriptions = new PriorityQueue<>();
		//send subscribe acknowledge to the OBD device
		SubscribeAckMessage subscribeAckMessage = new SubscribeAckMessage();
		subscribeAckMessage.setPacketId(subscribeMessage.getPacketId());
		subscribeMessage.getSubscriptions().forEach(deCouple -> {
			QosType qosType = QosType.valueOf(deCouple.getQos());
			Subscription newSubscription = new Subscription(clientId, deCouple.getTopicFilter(), qosType);
			boolean valid = clientSession.subscribe(newSubscription);
			subscribeAckMessage.addType(valid ? qosType : QosType.FAILURE);
			if (valid) {
				newSubscriptions.add(newSubscription);
			}
		});
		log.info("Subscribe acknowledgement for the packet <{}>", subscribeMessage.getPacketId());
		if (log.isTraceEnabled()) {
			//todo print the dump tree of the all subscriptions
			log.trace("subscription trace tree {}");
		}
		newSubscriptions.forEach(subscription ->{
			log.info("Subscribing {} to one channel", subscription);
			//todo impl the subscription to only one channel
		});
		channel.writeAndFlush(subscribeAckMessage);
		//todo publish the persisted packets in session also!
	}

	private void activeTheSession(ClientSession clientSession) {
		if (clientSession == null) {
			log.warn("no session specified!");
			return;
		}
		String clientId = clientSession.getClientId();
		if (mqttClients.containsKey(clientId)) {
			clientSession.setActive(true);
		}
	}
}
