package com.metao.mqtt.redisson;

import com.metao.mqtt.repositories.MessagesStore;
import com.metao.mqtt.repositories.SessionsStore;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Mehrdad A.Karami at 20/8/19
 */
public class RedissonPersistentStore {

    public Map<String, Object> redissonCache = new ConcurrentHashMap<>();

    private static final Logger LOG = LoggerFactory.getLogger(RedissonPersistentStore.class);

    private RedissonClient db;

    public RedissonPersistentStore(Config config) {
        db = Redisson.create(config);
    }

    /**
     * Factory method to create message store backed by MapDB
     */
    public MessagesStore messagesStore() {
        MessagesStore msgStore = new RedissonMessageStore(db, redissonCache);
        msgStore.initStore();
        return msgStore;
    }

    public SessionsStore sessionsStore(MessagesStore msgStore) {
        SessionsStore sessionsStore = new RedissonSessionsStore(db, redissonCache, msgStore);
        sessionsStore.initStore();
        return sessionsStore;
    }

    public void close() {
        db.shutdown();
    }
}
