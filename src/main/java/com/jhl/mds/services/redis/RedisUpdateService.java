package com.jhl.mds.services.redis;

import com.jhl.mds.dto.PairOfMap;
import com.jhl.mds.dto.migration.MySQL2RedisMigrationDTO;
import com.jhl.mds.util.pipeline.PipeLineTaskRunner;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.Map;
import java.util.function.Consumer;

@Service
public class RedisUpdateService implements PipeLineTaskRunner<MySQL2RedisMigrationDTO, PairOfMap, Boolean> {

    private RedisConnectionPool redisConnectionPool;

    public RedisUpdateService(
            RedisConnectionPool redisConnectionPool
    ) {
        super();
        this.redisConnectionPool = redisConnectionPool;
    }

    @Override
    public void execute(MySQL2RedisMigrationDTO context, PairOfMap input, Consumer<Boolean> next, Consumer<Exception> errorHandler) throws Exception {
        Jedis jedis = redisConnectionPool.getConnection(context.getTarget());

        switch (context.getRedisKeyType()) {
            case STRING:
                Map<String, Object> second = input.getSecond();
                jedis.set(String.valueOf(second.get("key")), String.valueOf(second.get("value")));
                break;
        }

        jedis.close();
        next.accept(true);
    }
}
