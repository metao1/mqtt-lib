package de.vispiron.carsync.mqtt;

import de.vispiron.carsync.mqtt.pack.MqttConnectResult;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.util.concurrent.Future;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * @author Mehrdad A.Karami at 3/8/19
 **/

public class MqttSubscribeTest extends MqttConnectTest {

	@Test
	public void subscribeToTheMqttServerTest() throws InterruptedException, ExecutionException, TimeoutException {
		Future<MqttConnectResult> localhost = mqttClient.connect(mqttHost, mqttPort);
		assertThat(localhost).isNotNull();
		Channel channel = localhost.get(timeout, TimeUnit.MILLISECONDS).getCloseFuture().channel();
		assertThat(channel).isNotNull();
		mqttClient.subscribeToTopic("/topic", mqttHandlerTest, MqttQoS.AT_LEAST_ONCE)
				.get(timeout, TimeUnit.MILLISECONDS);
		assertThat(mqttClient.isConnected()).isTrue();
		mqttClient.getPendingSubscriptions().forEach((key, value) -> assertThat(value.getTopic()).isNotEmpty());
	}


}
