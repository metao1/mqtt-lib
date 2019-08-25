package com.metao.mqtt.server;

public interface Authorizator {
    boolean canWrite(String topic, String user, String client);

    boolean canRead(String topic, String user, String client);
}
