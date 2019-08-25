package com.metao.mqtt.models.complex;

public class InterceptUnsubscribeMessage {
    private final String topicFilter;
    private final String clientID;
    private final String username;

    public InterceptUnsubscribeMessage(String topicFilter, String clientID, String username) {
        this.topicFilter = topicFilter;
        this.clientID = clientID;
        this.username = username;
    }

    public String getTopicFilter() {
        return topicFilter;
    }

    public String getClientID() {
        return clientID;
    }

    public String getUsername() {
        return username;
    }
}
