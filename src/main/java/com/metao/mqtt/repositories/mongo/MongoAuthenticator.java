package com.metao.mqtt.repositories.mongo;

import com.metao.mqtt.server.Authenticator;
import com.metao.mqtt.repositories.mongo.model.Product;
import com.metao.mqtt.repositories.mongo.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;

import java.util.List;

/**
 * @author Mehrdad A.Karami at 3/5/19
 */
public class MongoAuthenticator implements Authenticator {

    private static final Logger LOG = LoggerFactory.getLogger(MongoAuthenticator.class);

    @Autowired
    ProductRepository repository;

    @Override
    public boolean checkValid(String clientId, String username, byte[] password, boolean allowZeroByteClientId) {

        Product product = repository.findByUsername(username);

        if (product == null) {
            return false;
        }
        PasswordEncoder passwordEncoder = new StandardPasswordEncoder(product.getSalt());

        if (!passwordEncoder.matches(new String(password), product.getPassword())) {
            return false;
        }

        if (!allowZeroByteClientId && !product.getClients().contains(clientId)) {
            return false;
        }

        return true;
    }

}
