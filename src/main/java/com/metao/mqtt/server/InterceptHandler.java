package com.metao.mqtt.server;

import com.metao.mqtt.models.InterceptAcknowledgedMessage;
import com.metao.mqtt.models.complex.*;
import com.metao.mqtt.models.complex.*;

public interface InterceptHandler {

    void onConnect(InterceptConnectMessage msg);

    void onDisconnect(InterceptDisconnectMessage msg);

    void onPublish(InterceptPublishMessage msg);

    void onSubscribe(InterceptSubscribeMessage msg);

    void onUnsubscribe(InterceptUnsubscribeMessage msg);

    void onMessageAcknowledged(InterceptAcknowledgedMessage msg);
}
