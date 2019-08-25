package com.metao.mqtt.server;

import com.metao.mqtt.models.BytesMetrics;

import java.util.concurrent.atomic.AtomicLong;

public class BytesMetricsCollector {

    private AtomicLong readBytes = new AtomicLong();
    private AtomicLong wroteBytes = new AtomicLong();

    public BytesMetrics computeMetrics() {
        BytesMetrics allMetrics = new BytesMetrics();
        allMetrics.incrementRead(readBytes.get());
        allMetrics.incrementWrote(wroteBytes.get());
        return allMetrics;
    }

    public void sumReadBytes(long count) {
        readBytes.getAndAdd(count);
    }

    public void sumWroteBytes(long count) {
        wroteBytes.getAndAdd(count);
    }
}
