package de.vispiron.carsync.mqtt.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

/**
 * @author Mehrdad A.Karami at 3/7/19
 **/

@PropertySource("classpath:application.properties")
public class RedisProperties {

	@Value("${mqtt.redis.host}")
	public String address;

	@Value("${mqtt.redis.port}")
	public int port;

	@Value("${mqtt.redis.password}")
	public String password;

	@Value("${mqtt.redis.database")
	public String database;

	public String getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}

	public String getPassword() {
		return password;
	}

	public String getDatabase() {
		return database;
	}
}
