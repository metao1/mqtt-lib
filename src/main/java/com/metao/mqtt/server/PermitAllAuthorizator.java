package com.metao.mqtt.server;

public class PermitAllAuthorizator implements Authorizator {
    @Override
    public boolean canWrite(String topic, String user, String client) {
        return true;
    }

    @Override
    public boolean canRead(String topic, String user, String client) {
        return true;
    }
}
