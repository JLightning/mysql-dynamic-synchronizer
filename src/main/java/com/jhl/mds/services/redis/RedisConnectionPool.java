package com.jhl.mds.services.redis;

import com.jhl.mds.dto.RedisServerDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@Slf4j
public class RedisConnectionPool {

    private static final int MAX_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private final Random rand = new Random();
    private Map<RedisServerDTO, JedisPool> connections = new HashMap<>();

    public Jedis getConnection(RedisServerDTO dto) {
        if (!connections.containsKey(dto)) {
            connections.put(dto, new JedisPool(new JedisPoolConfig(), dto.getHost(), Integer.parseInt(dto.getPort())));
        }
        return connections.get(dto).getResource();
    }

    @PreDestroy
    public void destroy() {
        for (Map.Entry<RedisServerDTO, JedisPool> entry : connections.entrySet()) {
            log.info("Close connection for redis server: {}", entry.getKey());
            entry.getValue().close();
        }
    }
}
