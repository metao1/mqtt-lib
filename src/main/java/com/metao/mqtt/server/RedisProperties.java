package com.metao.mqtt.server;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Mehrdad A.Karami at 3/7/19
 **/

@ConfigurationProperties(prefix = "mqtt.redis", ignoreUnknownFields = true)
public class RedisProperties {

    public String host;

    public int port;

    public String password;

    public String database;

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getPassword() {
        return password;
    }

    public String getDatabase() {
        return database;
    }
}
