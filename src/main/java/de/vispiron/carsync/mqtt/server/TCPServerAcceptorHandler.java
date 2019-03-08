package de.vispiron.carsync.mqtt.server;

import de.vispiron.carsync.mqtt.utils.MqttFactoryBuilder;
import de.vispiron.carsync.mqtt.utils.Utils;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mehrdad A.Karami at 2/25/19
 **/

@PropertySource("classpath:application.properties")
public class TCPServerAcceptorHandler {

	@Value("${mqtt.host}")
	public String MQTT_HOST;

	@Value("${mqtt.port}")
	public int MQTT_PORT;

	private EventLoopGroup tcpGroup;
	private EventLoopGroup workerGroup;

	private Map<String, MqttFactoryBuilder.GeneralServer> serverMap = new HashMap<>();

	@Autowired
	private TcpHandler tcpHandler;

	/**
	 * Initialize the MQTT Server for the Telemetry microservice
	 * @throws InterruptedException
	 */
	public void initialize() throws InterruptedException {
		tcpGroup = new NioEventLoopGroup();
		workerGroup = new NioEventLoopGroup();
		initializeTcpHandler(tcpGroup, workerGroup);
	}

	private void initializeTcpHandler(EventLoopGroup tcpGroup, EventLoopGroup workerGroup) throws InterruptedException {
		MqttFactoryBuilder.GeneralServer mqttTcpServer = new MqttFactoryBuilder()
				.tcpConnection(tcpGroup, workerGroup, MQTT_HOST, MQTT_PORT);
				//todo add ssl tcp connection here
		mqttTcpServer.build(tcpHandler);
		serverMap.put(Utils.generateUniqueId(), mqttTcpServer);
	}

	public Map<String, MqttFactoryBuilder.GeneralServer> getServerMap() {
		return serverMap;
	}
}
