package com.metao.mqtt;

import com.metao.mqtt.repositories.mongo.model.User;
import com.metao.mqtt.repositories.mongo.repository.UserRepository;
import com.metao.mqtt.server.MqttProperties;
import com.metao.mqtt.server.RedisProperties;
import com.metao.mqtt.server.TCPServerAcceptorHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.List;
import java.util.UUID;

@SpringBootApplication
@EnableConfigurationProperties({MqttProperties.class, RedisProperties.class})
public class MqttApplication implements CommandLineRunner {

    @Autowired
    TCPServerAcceptorHandler acceptor;

    @Autowired
    UserRepository userRepository;

    public static void main(String[] args) {
        SpringApplication.run(MqttApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        List<User> all = userRepository.findAll();
        for (final User user : all) {
            userRepository.deleteByUsername(user.getUsername());
        }
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setPassword("25de6fd7a33ced95274445c00055d770857eaca128be7b7c30d7ce7cde433367c37b9a465536299d");
        user.setUsername("mehrdad");
        user.setSalt("mehrdad");
        User save = userRepository.save(user);
        assert save.getId() != null;
        //acceptor.initialize();//initialize the mqtt server
    }
}
