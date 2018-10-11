package com.jhl.mds.services.redis;

import com.jhl.mds.dto.migration.MySQL2RedisMigrationDTO;
import com.jhl.mds.util.pipeline.PipeLineTaskRunner;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.Map;
import java.util.function.Consumer;

@Service
public class RedisDeleteService implements PipeLineTaskRunner<MySQL2RedisMigrationDTO, Map<String, Object>, Boolean> {

    private RedisConnectionPool redisConnectionPool;

    public RedisDeleteService(
            RedisConnectionPool redisConnectionPool
    ) {
        this.redisConnectionPool = redisConnectionPool;
    }

    @Override
    public void execute(MySQL2RedisMigrationDTO context, Map<String, Object> input, Consumer<Boolean> next, Consumer<Exception> errorHandler) throws Exception {
        Jedis jedis = redisConnectionPool.getConnection(context.getTarget());
        jedis.del(String.valueOf(input.get("key")));

        jedis.close();

        next.accept(true);
    }
}
