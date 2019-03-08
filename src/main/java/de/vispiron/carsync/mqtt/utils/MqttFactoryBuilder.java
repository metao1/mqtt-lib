package de.vispiron.carsync.mqtt.utils;

import de.vispiron.carsync.mqtt.coders.MQTTDecoder;
import de.vispiron.carsync.mqtt.coders.MQTTEncoder;
import de.vispiron.carsync.mqtt.server.IdleTimeoutHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.MalformedParametersException;

/**
 * @author Mehrdad A.Karami at 2/25/19
 **/

public class MqttFactoryBuilder {
	private static Logger log = LoggerFactory.getILoggerFactory().getLogger(MqttFactoryBuilder.class.getName());

	public GeneralServer tcpConnection(EventLoopGroup tcpGroup, EventLoopGroup workerGroup, String mqttHost,
			int mqttPort) {
		if (!Utils.isValid(mqttHost) || mqttPort <= 0) {
			throw new MalformedParametersException("The mqtt host or port not set, check your application.properties");
		}
		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.group(tcpGroup, workerGroup).channel(NioServerSocketChannel.class);
		return new TCPServer(bootstrap, mqttHost, mqttPort);
	}

	public abstract class GeneralServer {
		private boolean run;
		private final String host;
		private final int port;
		private final ServerBootstrap bootstrap;
		ChannelInboundHandlerAdapter handlerAdapter;

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
						setOptions(bootstrap);
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
	}

	private class TCPServer extends GeneralServer {
		final IdleTimeoutHandler idleTimeoutHandler = new IdleTimeoutHandler();

		TCPServer(ServerBootstrap bootstrap, String mqttHost, int mqttPort) {
			super(bootstrap, mqttHost, mqttPort);
		}

		@Override
		protected void setOptions(ServerBootstrap serverBootstrap) {
			serverBootstrap.option(ChannelOption.SO_BACKLOG, 128).option(ChannelOption.SO_REUSEADDR, true)
					.option(ChannelOption.TCP_NODELAY, true).childOption(ChannelOption.SO_KEEPALIVE, true);
		}

		@Override
		protected void setPipeLine(ChannelPipeline pipeline) {
			pipeline.addFirst("idleStateHandler", new IdleStateHandler(0, 0, 10))
					.addAfter("idleStateHandler", "idleEventHandler", idleTimeoutHandler)
					.addLast("decoder", new MQTTDecoder())
					.addLast("encoder", new MQTTEncoder())
					.addLast("handler", handlerAdapter);
			//			todo add mqtt decoders and metrics (bytes counter and packet counter)
		}
	}

}
