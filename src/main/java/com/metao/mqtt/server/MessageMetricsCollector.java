package com.metao.mqtt.server;

import com.metao.mqtt.models.MessageMetrics;

import java.util.concurrent.atomic.AtomicLong;

public class MessageMetricsCollector {
    private AtomicLong readMsgs = new AtomicLong();
    private AtomicLong wroteMsgs = new AtomicLong();

    public MessageMetrics computeMetrics() {
        MessageMetrics allMetrics = new MessageMetrics();
        allMetrics.incrementRead(readMsgs.get());
        allMetrics.incrementWrote(wroteMsgs.get());
        return allMetrics;
    }

    public void sumReadMessages(long count) {
        readMsgs.getAndAdd(count);
    }

    public void sumWroteMessages(long count) {
        wroteMsgs.getAndAdd(count);
    }
}
