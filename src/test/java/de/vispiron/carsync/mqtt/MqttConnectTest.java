package de.vispiron.carsync.mqtt;

import de.vispiron.carsync.mqtt.pack.MqttClientImpl;
import de.vispiron.carsync.mqtt.pack.MqttConnectResult;
import de.vispiron.carsync.mqtt.pack.MqttHandler;
import de.vispiron.carsync.mqtt.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.util.concurrent.Future;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static io.netty.handler.codec.mqtt.MqttConnectReturnCode.CONNECTION_ACCEPTED;
import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * @author Mehrdad A.Karami at 3/8/19
 **/

@RunWith(SpringRunner.class)
@SpringBootTest
public class MqttConnectTest extends MqttSetupTest {

	protected static Logger log = LoggerFactory.getLogger(MqttConnectTest.class);

	@Value("${mqtt.host}")
	public String mqttHost;

	@Value("${mqtt.port}")
	public Integer mqttPort;

	protected MqttClientImpl mqttClient;

	//Timeout for the Future object until it get completed
	protected final long timeout = 5000;
	protected MqttHandlerTest mqttHandlerTest;

	protected static class MqttHandlerTest implements MqttHandler {

		@Override
		public void onMessage(String topic, ByteBuf payload) {
			log.info("new message : topic <{}> & payload <{}> ", topic, Utils.decodeByteBuffToString(payload));
		}
	}

	@Before
	public void setup() {
		mqttHandlerTest = new MqttHandlerTest();
		mqttClient = new MqttClientImpl(mqttHandlerTest);
	}

	@Test
	public void connectToMqttServerTest() throws InterruptedException, ExecutionException, TimeoutException {
		Future<MqttConnectResult> localhost = mqttClient.connect(mqttHost, mqttPort);
		assertThat(localhost).isNotNull();
		assertThat(localhost.get(timeout, TimeUnit.MILLISECONDS).getReturnCode()).isNotNull();
		assertThat(localhost.get(timeout, TimeUnit.MILLISECONDS).getReturnCode()).isEqualTo(CONNECTION_ACCEPTED);
		assertThat(localhost.get(timeout, TimeUnit.MILLISECONDS).isSuccess()).isTrue();
	}

	@Test
	public void reconnectToTheMqttServerTest() throws InterruptedException, ExecutionException, TimeoutException {
		Future<MqttConnectResult> localhost = mqttClient.connect(mqttHost, mqttPort);
		assertThat(localhost).isNotNull();
		Channel channel = localhost.get(timeout, TimeUnit.MILLISECONDS).getCloseFuture().channel();
		assertThat(channel).isNotNull();
		Future<MqttConnectResult> reconnect = mqttClient.reconnect();
		assertThat(reconnect.get(timeout, TimeUnit.MILLISECONDS)).isNotNull();
		assertThat(reconnect.get(timeout, TimeUnit.MILLISECONDS).getReturnCode()).isNotNull();
		assertThat(reconnect.get(timeout, TimeUnit.MILLISECONDS).getCloseFuture().channel()).isNotNull();
		assertThat(reconnect.get(timeout, TimeUnit.MILLISECONDS).getReturnCode()).isEqualTo(CONNECTION_ACCEPTED);
	}
}
