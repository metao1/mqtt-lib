package com.metao.mqtt.repositories.mongo;

import com.metao.mqtt.server.Authenticator;
import com.metao.mqtt.repositories.mongo.model.User;
import com.metao.mqtt.repositories.mongo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;

/**
 * @author Mehrdad A.Karami at 3/5/19
 */
public class MongoAuthenticator implements Authenticator {

    private static final Logger LOG = LoggerFactory.getLogger(MongoAuthenticator.class);

    @Autowired
    UserRepository repository;

    @Override
    public boolean checkValid(String clientId, String username, byte[] password, boolean allowZeroByteClientId) {

        User user = repository.findByUsername(username);

        if (user == null) {
            return false;
        }
        PasswordEncoder passwordEncoder = new StandardPasswordEncoder(user.getSalt());

        if (!passwordEncoder.matches(new String(password), user.getPassword())) {
            return false;
        }

        if (!allowZeroByteClientId && !user.getClients().contains(clientId)) {
            return false;
        }

        return true;
    }

}
