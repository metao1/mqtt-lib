package com.metao.mqtt.server;

import com.metao.mqtt.models.ConnectPacket;
import com.metao.mqtt.models.InterceptAcknowledgedMessage;
import com.metao.mqtt.models.PublishPacket;
import com.metao.mqtt.models.Subscription;
import com.metao.mqtt.models.complex.*;
import com.metao.mqtt.models.complex.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BrokerInterceptor implements Interceptor {
    private final List<InterceptHandler> handlers;
    private final ExecutorService executor;

    public BrokerInterceptor(List<InterceptHandler> handlers) {
        this.handlers = new CopyOnWriteArrayList<>(handlers);
        executor = Executors.newFixedThreadPool(1);
    }

    /**
     * Shutdown graciously the executor service
     */
    void stop() {
        executor.shutdown();
    }

    @Override
    public void notifyClientConnected(final ConnectPacket msg) {
        for (final InterceptHandler handler : this.handlers) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    handler.onConnect(new InterceptConnectMessage(msg));
                }
            });
        }
    }

    @Override
    public void notifyClientDisconnected(final String clientID, final String username) {
        for (final InterceptHandler handler : this.handlers) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    handler.onDisconnect(new InterceptDisconnectMessage(clientID, username));
                }
            });
        }
    }

    @Override
    public void notifyTopicPublished(final PublishPacket msg, final String clientID, final String username) {
        for (final InterceptHandler handler : this.handlers) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    handler.onPublish(new InterceptPublishMessage(msg, clientID, username));
                }
            });
        }
    }

    @Override
    public void notifyTopicSubscribed(final Subscription sub, final String username) {
        for (final InterceptHandler handler : this.handlers) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    handler.onSubscribe(new InterceptSubscribeMessage(sub, username));
                }
            });
        }
    }

    @Override
    public void notifyTopicUnsubscribed(final String topic, final String clientID, final String username) {
        for (final InterceptHandler handler : this.handlers) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    handler.onUnsubscribe(new InterceptUnsubscribeMessage(topic, clientID, username));
                }
            });
        }
    }

    @Override
    public void notifyMessageAcknowledged(final InterceptAcknowledgedMessage msg) {
        for (final InterceptHandler handler : this.handlers) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    handler.onMessageAcknowledged(msg);
                }
            });
        }
    }

    @Override
    public boolean addInterceptHandler(InterceptHandler interceptHandler) {
        return this.handlers.add(interceptHandler);
    }

    @Override
    public boolean removeInterceptHandler(InterceptHandler interceptHandler) {
        return this.handlers.remove(interceptHandler);
    }
}
