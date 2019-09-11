package com.metao.mqtt;

import com.metao.mqtt.server.TCPServerAcceptorHandler;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.charset.Charset;

import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * @author Mehrdad A.Karami at 2/28/19
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@PropertySource("classpath:application-test.properties")
public class MqttSetupTest {

	@Value("${mqtt.host}")
	String mqttHost;
	@Value("${mqtt.port}")
	private int mqttPort;

	@Autowired
	protected TCPServerAcceptorHandler tcpServerAcceptorHandler;

	@Test
	public void setTcpServerAcceptorTest() {
		assertThat(tcpServerAcceptorHandler).isNotNull();
	}

	@Test
	public void mqttServerInitializationTest() {
		assertThat(tcpServerAcceptorHandler.getServerMap().size()).isEqualTo(1);
	}

	@Test
	public void tcpServerEndpointsTest() {
		FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
		httpRequest.setUri(mqttHost.concat(":").concat(mqttPort + ""));
		String stringResponseContent = httpRequest.content().toString(Charset.defaultCharset());
		assertThat(stringResponseContent).isNotNull();
	}

}
