package com.metao.mqtt.models;

/**
 * @author Mehrdad A.Karami at 25/08/19
 **/
public class MessageMetrics {
    private long messagesRead = 0;
    private long messageWrote = 0;

    public void incrementRead(long numMessages) {
        messagesRead += numMessages;
    }

    public void incrementWrote(long numMessages) {
        messageWrote += numMessages;
    }

    public long messagesRead() {
        return messagesRead;
    }

    public long messagesWrote() {
        return messageWrote;
    }
}