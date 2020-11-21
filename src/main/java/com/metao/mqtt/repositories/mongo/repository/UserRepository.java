package com.metao.mqtt.repositories.mongo.repository;

import com.metao.mqtt.repositories.mongo.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * @author Mehrdad A.Karami at 3/5/19
 */
public interface UserRepository extends MongoRepository<User, String> {
    User findByUsername(String username);

    void deleteByUsername(String username);
}
