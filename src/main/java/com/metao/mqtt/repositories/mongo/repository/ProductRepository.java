package com.metao.mqtt.repositories.mongo.repository;

import com.metao.mqtt.repositories.mongo.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * @author Mehrdad A.Karami at 3/5/19
 */
public interface ProductRepository extends MongoRepository<Product, String> {
    Product findByUsername(String username);

    @Override
    List<Product> findAll();

    void deleteByUsername(String username);
}
