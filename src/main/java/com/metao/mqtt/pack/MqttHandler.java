package com.metao.mqtt.pack;

import io.netty.buffer.ByteBuf;

import java.io.UnsupportedEncodingException;

public interface MqttHandler {

    void onMessage(String topic, ByteBuf payload) throws UnsupportedEncodingException;
}
