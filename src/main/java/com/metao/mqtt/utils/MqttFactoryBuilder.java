package com.metao.mqtt.utils;

import com.metao.mqtt.coders.decoders.MQTTDecoder;
import com.metao.mqtt.coders.encoders.MQTTEncoder;
import com.metao.mqtt.models.BytesMetrics;
import com.metao.mqtt.models.MessageMetrics;
import com.metao.mqtt.server.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.lang.reflect.MalformedParametersException;

/**
 * @author Mehrdad A.Karami at 2/25/19
 **/
@Component
public class MqttFactoryBuilder {
    private static Logger log = LoggerFactory.getILoggerFactory().getLogger(MqttFactoryBuilder.class.getName());

    public static GeneralServer makeTcpConnection(EventLoopGroup tcpGroup, EventLoopGroup workerGroup, String mqttHost,
                                                  int mqttPort) {
        if (!Utils.isValid(mqttHost) || mqttPort <= 0) {
            throw new MalformedParametersException("The mqtt host or port not set, check your application.properties");
        }
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(tcpGroup, workerGroup).channel(NioServerSocketChannel.class);
        return new TCPServer(bootstrap, mqttHost, mqttPort);
    }

    public abstract static class GeneralServer {
        private boolean run;
        private final String host;
        private final int port;
        private final ServerBootstrap bootstrap;
        ChannelInboundHandlerAdapter handlerAdapter;
        BytesMetricsCollector bytesMetricsCollector = new BytesMetricsCollector();
        MessageMetricsCollector metricsCollector = new MessageMetricsCollector();

        GeneralServer(ServerBootstrap bootstrap, String host, int port) {
            this.host = host;
            this.bootstrap = bootstrap;
            this.port = port;
            init();
        }

        protected abstract void setOptions(ServerBootstrap serverBootstrap);

        protected boolean isRun() {
            return run;
        }

        private void init() {
            try {
                this.bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        setPipeLine(ch.pipeline());
                        bootstrap.option(ChannelOption.SO_BACKLOG, 128).option(ChannelOption.SO_REUSEADDR, true)
                            .option(ChannelOption.TCP_NODELAY, true).childOption(ChannelOption.SO_KEEPALIVE, true);
                    }
                });
            } catch (Exception e) {
                log.error("Could not init the server cause {}", e.getCause().getMessage());
                run = false;
            }
        }

        protected abstract void setPipeLine(ChannelPipeline pipeline);

        public void build(final ChannelInboundHandlerAdapter handlerAdapter) {
            try {
                this.handlerAdapter = handlerAdapter;
                ChannelFuture channelFuture = this.bootstrap.bind(host, port);
                channelFuture.sync();
                log.info("Server bound host:{}, port: {}", host, port);
                run = true;
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    log.error("Could not init the server cause: Address in use {}", e.getMessage());
                } else {
                    log.error("Could not init the server cause {}", e.getMessage());
                }
                run = false;
            }
        }

        public void handleClosePipeLine(EventLoopGroup workerGroup, EventLoopGroup parentGroup) {
            if (workerGroup == null) {
                throw new IllegalStateException("Invoked close on an Acceptor that wasn't initialized");
            }
            if (parentGroup == null) {
                throw new IllegalStateException("Invoked close on an Acceptor that wasn't initialized");
            }

            Future workerWaiter = workerGroup.shutdownGracefully();
            Future bossWaiter = parentGroup.shutdownGracefully();

            try {
                workerWaiter.await(100);
            } catch (InterruptedException iex) {
                throw new IllegalStateException(iex);
            }

            try {
                bossWaiter.await(100);
            } catch (InterruptedException iex) {
                throw new IllegalStateException(iex);
            }

            MessageMetrics metrics = metricsCollector.computeMetrics();
            log.info("Msg read: {}, msg wrote: {}", metrics.messagesRead(), metrics.messagesWrote());

            BytesMetrics bytesMetrics = bytesMetricsCollector.computeMetrics();
            log.info(String.format("Bytes read: %d, bytes wrote: %d", bytesMetrics.readBytes(), bytesMetrics.wroteBytes()));

            log.info("mqtt server closed");
        }
    }

    private static class TCPServer extends GeneralServer {
        final IdleTimeoutHandler idleTimeoutHandler = new IdleTimeoutHandler();

        TCPServer(ServerBootstrap bootstrap, String mqttHost, int mqttPort) {
            super(bootstrap, mqttHost, mqttPort);
        }

        @Override
        protected void setOptions(ServerBootstrap serverBootstrap) {

        }

        @Override
        protected void setPipeLine(ChannelPipeline pipeline) {
            pipeline.addFirst("idleStateHandler", new IdleStateHandler(0, 0, 10))
                .addAfter("idleStateHandler", "idleEventHandler", idleTimeoutHandler)
                //.addFirst("bytemetrics", new BytesMetricsHandler(bytesMetricsCollector))
                .addLast("decoder", new MQTTDecoder())
                .addLast("encoder", new MQTTEncoder())
                //.addLast("metrics", new MessageMetricsHandler(metricsCollector))
                .addLast("handler", handlerAdapter);
        }
    }

}
