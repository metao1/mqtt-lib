package com.metao.mqtt.models;

public class BytesMetrics {
    private long readBytes = 0;
    private long wroteBytes = 0;

    public void incrementRead(long numBytes) {
        readBytes += numBytes;
    }

    public void incrementWrote(long numBytes) {
        wroteBytes += numBytes;
    }

    public long readBytes() {
        return readBytes;
    }

    public long wroteBytes() {
        return wroteBytes;
    }
}