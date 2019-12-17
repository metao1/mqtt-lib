package com.metao.mqtt;

import com.metao.mqtt.repositories.mongo.model.Product;
import com.metao.mqtt.repositories.mongo.repository.ProductRepository;
import com.metao.mqtt.server.MqttProperties;
import com.metao.mqtt.server.RedisProperties;
import com.metao.mqtt.server.TCPServerAcceptorHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.crypto.password.StandardPasswordEncoder;

import java.util.List;
import java.util.UUID;

@SpringBootApplication
@EnableConfigurationProperties({MqttProperties.class, RedisProperties.class})
public class MqttApplication implements CommandLineRunner {

    @Autowired
    TCPServerAcceptorHandler acceptor;

    @Autowired
    ProductRepository productRepository;

    public static void main(String[] args) {
        SpringApplication.run(MqttApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        List<Product> all = productRepository.findAll();
        for (final Product product : all) {
            productRepository.deleteByUsername(product.getUsername());
        }
        Product product = new Product();
        product.setId(UUID.randomUUID().toString());
        product.setPassword("25de6fd7a33ced95274445c00055d770857eaca128be7b7c30d7ce7cde433367c37b9a465536299d");
        product.setUsername("mehrdad");
        product.setSalt("mehrdad");
        Product save = productRepository.save(product);
        assert save.getId() != null;
        //acceptor.initialize();//initialize the mqtt server
    }
}
