package com.metao.mqtt.server;

public interface Authenticator {

    boolean checkValid(String clientId, String username, byte[] password, boolean allowZeroByteClientId);
}
