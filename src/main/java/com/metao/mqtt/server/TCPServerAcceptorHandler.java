package com.metao.mqtt.server;

import com.metao.mqtt.utils.MqttFactoryBuilder;
import com.metao.mqtt.utils.Utils;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mehrdad A.Karami at 2/25/19
 **/

public class TCPServerAcceptorHandler {

    private EventLoopGroup tcpGroup;
    private EventLoopGroup workerGroup;

    private Map<String, MqttFactoryBuilder.GeneralServer> serverMap = new HashMap<>();

    @Autowired
    private TcpHandler tcpHandler;

    @Autowired
    MqttProperties mqttProperties;

    private MqttFactoryBuilder.GeneralServer mqttTcpServer;
    private boolean connected;

    /**
     * Initialize the MQTT Server for the mqtt microservice
     *
     * @throws InterruptedException
     */
    @Autowired
    public void initialize() throws InterruptedException {
        tcpGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        initializeTcpHandler(tcpGroup, workerGroup);
    }

    @PreDestroy
    public void destroy() {
        mqttTcpServer.handleClosePipeLine(tcpGroup, workerGroup);
        connected = false;
    }

    private void initializeTcpHandler(EventLoopGroup tcpGroup, EventLoopGroup workerGroup) {
        mqttTcpServer = MqttFactoryBuilder
                .makeTcpConnection(tcpGroup, workerGroup, mqttProperties.getHost(), mqttProperties.getPort());
        //todo add ssl tcp connection here
        mqttTcpServer.build(tcpHandler);
        serverMap.put(Utils.generateUniqueId(), mqttTcpServer);
        connected = true;
    }

    public Map<String, MqttFactoryBuilder.GeneralServer> getServerMap() {
        return serverMap;
    }

    public boolean isConnected() {
        return connected;
    }
}
