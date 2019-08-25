package com.metao.mqtt.models.complex;

import com.metao.mqtt.models.QosType;
import com.metao.mqtt.models.Subscription;

public class InterceptSubscribeMessage {
    private final Subscription subscription;
    private final String username;

    public InterceptSubscribeMessage(Subscription subscription, String username) {
        this.subscription = subscription;
        this.username = username;
    }

    public String getClientID() {
        return subscription.getClientId();
    }

    public QosType getRequestedQos() {
        return subscription.getRequestedQos();
    }

    public String getTopicFilter() {
        return subscription.getTopicFilter();
    }

    public String getUsername() {
        return username;
    }
}
