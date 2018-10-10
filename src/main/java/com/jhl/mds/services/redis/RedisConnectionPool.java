package com.jhl.mds.services.redis;

import com.jhl.mds.dto.RedisServerDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import javax.annotation.PreDestroy;
import java.util.*;

@Service
@Slf4j
public class RedisConnectionPool {

    private static final int MAX_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private final Random rand = new Random();
    private Map<RedisServerDTO, List<Jedis>> connections = new HashMap<>();

    public Jedis getConnection(RedisServerDTO dto) {
        if (!connections.containsKey(dto)) {
            List<Jedis> conns = new ArrayList<>();
            for (int i = 0; i < MAX_POOL_SIZE; i++) {
                conns.add(new Jedis(dto.getHost(), Integer.parseInt(dto.getPort())));
            }
            connections.put(dto, conns);
        }
        return connections.get(dto).get(rand.nextInt(MAX_POOL_SIZE));
    }

    @PreDestroy
    public void destroy() {
        for (Map.Entry<RedisServerDTO, List<Jedis>> entry : connections.entrySet()) {
            log.info("Close connection for redis server: {}", entry.getKey());
            for (Jedis conn : entry.getValue()) {
                conn.close();
            }
        }
    }
}
