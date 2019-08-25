package com.metao.mqtt.models;

/**
 * Model class that maintains information about which Topic a certain ClientID is subscribed
 *
 * @author Mehrdad A.Karami at 3/5/19
 **/

public class Subscription {
    QosType requestedQos; //max QoS acceptable
    String clientId;
    String topicFilter;

    public Subscription() {
    }

    public Subscription(String clientId, String topicFilter, QosType requestedQos) {
        this.requestedQos = requestedQos;
        this.clientId = clientId;
        this.topicFilter = topicFilter;
    }

    public Subscription(Subscription orig) {
        this.requestedQos = orig.requestedQos;
        this.clientId = orig.clientId;
        this.topicFilter = orig.topicFilter;
    }

    public String getClientId() {
        return clientId;
    }

    public QosType getRequestedQos() {
        return requestedQos;
    }

    public String getTopicFilter() {
        return topicFilter;
    }

    public void setRequestedQos(QosType requestedQos) {
        this.requestedQos = requestedQos;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setTopicFilter(String topicFilter) {
        this.topicFilter = topicFilter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Subscription that = (Subscription) o;

        if (clientId != null ? !clientId.equals(that.clientId) : that.clientId != null) return false;
        return !(topicFilter != null ? !topicFilter.equals(that.topicFilter) : that.topicFilter != null);

    }

    @Override
    public int hashCode() {
        int result = clientId != null ? clientId.hashCode() : 0;
        result = 31 * result + (topicFilter != null ? topicFilter.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("[filter:%s, cliID: %s, qos: %s, active: %s]", this.topicFilter, this.clientId, this.requestedQos);
    }

    @Override
    public Subscription clone() {
        try {
            return (Subscription) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public ClientTopicCouple asClientTopicCouple() {
        return new ClientTopicCouple(this.clientId, this.topicFilter);
    }
}
