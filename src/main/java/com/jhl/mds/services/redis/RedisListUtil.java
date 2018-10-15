package com.jhl.mds.services.redis;

import com.jhl.mds.dto.RedisServerDTO;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class RedisListUtil {

    private RedisConnectionPool redisConnectionPool;

    public RedisListUtil(
            RedisConnectionPool redisConnectionPool
    ) {
        this.redisConnectionPool = redisConnectionPool;
    }

    public long findValueInList(RedisServerDTO redisServerDTO, String key, String value) {
        try (Jedis jedis = redisConnectionPool.getConnection(redisServerDTO)) {
            Long len = jedis.llen(key);
            if (len == null) return -1;

            List<String> values = jedis.lrange(key, 0, len);
            for (int i = 0; i < len; i++) {
                String valueInRedis = values.get(i);
                if (valueInRedis.equals(value)) {
                    return i;
                }
            }
            return -1;
        }
    }
}
