package com.metao.mqtt.server;

import com.metao.mqtt.repositories.mongo.MongoAuthenticator;
import com.metao.mqtt.redisson.RedissonPersistentStore;
import com.metao.mqtt.repositories.MessagesStore;
import com.metao.mqtt.repositories.SessionsStore;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.ArrayList;

/**
 * @author Mehrdad A.Karami at 3/7/19
 **/

@Configuration
public class ServerConfiguration {

    @Autowired
    RedisProperties redisProperties;

    @Bean
    Authenticator authenticator() {
        return new MongoAuthenticator();
    }

    @Bean
    Authorizator authorizator() {
        return new PermitAllAuthorizator();
    }

    @Bean(destroyMethod = "close")
    RedissonPersistentStore persistenceStore(RedisProperties redisProperties) {
        Config config = new Config();
        SingleServerConfig singleServerConfig = config.useSingleServer();
        singleServerConfig.setAddress("redis://" + redisProperties.getHost()
            + ":" + redisProperties.getPort());
        //singleServerConfig.setDatabase(redisProperties.getDatabase());
        if (!StringUtils.isEmpty(redisProperties.getPassword())) {
            singleServerConfig.setPassword(redisProperties.getPassword());
        }
        return new RedissonPersistentStore(config);
    }

    @Bean
    MessagesStore messagesStore(RedissonPersistentStore persistenceStore) {
        return persistenceStore.messagesStore();
    }

    @Bean
    SessionsStore sessionsStore(RedissonPersistentStore persistenceStore, MessagesStore messagesStore) {
        return persistenceStore.sessionsStore(messagesStore);
    }

    @Bean(destroyMethod = "stop")
    BrokerInterceptor brokerInterceptor() {
        return new BrokerInterceptor(new ArrayList<>());
    }

}
