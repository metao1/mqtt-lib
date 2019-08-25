package com.metao.mqtt.server;

import com.metao.mqtt.models.ConnectPacket;
import com.metao.mqtt.models.InterceptAcknowledgedMessage;
import com.metao.mqtt.models.PublishPacket;
import com.metao.mqtt.models.Subscription;

public interface Interceptor {

    void notifyClientConnected(ConnectPacket msg);

    void notifyClientDisconnected(String clientID, String username);

    void notifyTopicPublished(PublishPacket msg, String clientID, final String username);

    void notifyTopicSubscribed(Subscription sub, final String username);

    void notifyTopicUnsubscribed(String topic, String clientID, final String username);

    void notifyMessageAcknowledged(InterceptAcknowledgedMessage msg);

    boolean addInterceptHandler(InterceptHandler interceptHandler);

    boolean removeInterceptHandler(InterceptHandler interceptHandler);
}
