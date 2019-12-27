package com.metao.mqtt.server;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Mehrdad A.Karami at 3/7/19
 **/

@ConfigurationProperties(prefix = "mqtt", ignoreUnknownFields = true)
public class MqttProperties {

    private Boolean allowAnonymous = false;

    private Boolean allowZeroByteClientId = true;

    private Integer connectTimeout = 10;

    private Integer bossCount = 0;

    private Integer workCount = 0;

    private boolean passwordEnabled;

    private String url;

    private String host;

    private Integer port;

    private Integer mqttsPort;

    private Integer wsPort;

    private Integer wssPort;

    private Boolean needsClientAuth = false;

    private Boolean mqttEnable = true;

    private Boolean mqttsEnable = false;

    private Boolean wsEnable = false;

    private Boolean wssEnable = false;

    private String username;

    private String password;

    public Boolean getAllowAnonymous() {
        return allowAnonymous;
    }

    public void setAllowAnonymous(Boolean allowAnonymous) {
        this.allowAnonymous = allowAnonymous;
    }

    public Boolean getAllowZeroByteClientId() {
        return allowZeroByteClientId;
    }

    public void setAllowZeroByteClientId(Boolean allowZeroByteClientId) {
        this.allowZeroByteClientId = allowZeroByteClientId;
    }

    public Integer getBossCount() {
        return bossCount;
    }

    public void setBossCount(Integer bossCount) {
        this.bossCount = bossCount;
    }

    public Integer getWorkCount() {
        return workCount;
    }

    public void setWorkCount(Integer workCount) {
        this.workCount = workCount;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getMqttsPort() {
        return mqttsPort;
    }

    public void setMqttsPort(Integer mqttsPort) {
        this.mqttsPort = mqttsPort;
    }

    public Integer getWsPort() {
        return wsPort;
    }

    public void setWsPort(Integer wsPort) {
        this.wsPort = wsPort;
    }

    public Integer getWssPort() {
        return wssPort;
    }

    public void setWssPort(Integer wssPort) {
        this.wssPort = wssPort;
    }

    public Boolean getMqttEnable() {
        return mqttEnable;
    }

    public void setMqttEnable(Boolean mqttEnable) {
        this.mqttEnable = mqttEnable;
    }

    public Boolean getMqttsEnable() {
        return mqttsEnable;
    }

    public void setMqttsEnable(Boolean mqttsEnable) {
        this.mqttsEnable = mqttsEnable;
    }

    public Boolean getWsEnable() {
        return wsEnable;
    }

    public void setWsEnable(Boolean wsEnable) {
        this.wsEnable = wsEnable;
    }

    public Boolean getWssEnable() {
        return wssEnable;
    }

    public void setWssEnable(Boolean wssEnable) {
        this.wssEnable = wssEnable;
    }

    public Boolean getNeedsClientAuth() {
        return needsClientAuth;
    }

    public void setNeedsClientAuth(Boolean needsClientAuth) {
        this.needsClientAuth = needsClientAuth;
    }

    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPasswordEnabled(boolean passwordEnabled) {
        this.passwordEnabled = passwordEnabled;
    }

    public boolean isPasswordEnabled(){
        return passwordEnabled;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
