package de.vispiron.carsync.mqtt.server;

import de.vispiron.carsync.mqtt.repositories.MessageStore;
import de.vispiron.carsync.mqtt.repositories.SessionStoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Mehrdad A.Karami at 3/7/19
 **/

@Configuration
public class ServerConfiguration {

	@Autowired
	RedisProperties redisProperties;

	@Bean(destroyMethod = "close")
	SessionStoreRepository sessionStoreRepository(RedisProperties redisProperties){
		return new SessionStoreRepository();
	}

	@Bean
	MessageStore messageStore(SessionStoreRepository sessionStoreRepository){
		return sessionStoreRepository.getMessageStore();
	}

	//todo create broker interceptor to close all connections when database is closed
}
