package de.vispiron.carsync.mqtt.configuration;

import de.vispiron.carsync.mqtt.server.RedisProperties;
import de.vispiron.carsync.mqtt.server.TCPServerAcceptorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Mehrdad A.Karami at 2/26/19
 **/
@Configuration
public class ServerConfig {

	@Bean
	TCPServerAcceptorHandler acceptor() {
		return new TCPServerAcceptorHandler();
	}

	@Bean
	RedisProperties redisProperties() {
		return new RedisProperties();
	}

}
