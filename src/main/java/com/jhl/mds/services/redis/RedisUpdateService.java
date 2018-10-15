package com.jhl.mds.services.redis;

import com.jhl.mds.dto.PairOfMap;
import com.jhl.mds.dto.migration.MySQL2RedisMigrationDTO;
import com.jhl.mds.util.pipeline.PipeLineTaskRunner;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.List;
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

        Map<String, Object> first = input.getFirst();
        Map<String, Object> second = input.getSecond();

        switchLabel:
        switch (context.getRedisKeyType()) {
            case STRING:
                jedis.set(String.valueOf(second.get("key")), String.valueOf(second.get("value")));
                break;
            case LIST:
                String key = String.valueOf(first.get("key"));
                String firstValue = String.valueOf(first.get("value"));
                String secondValue = String.valueOf(second.get("value"));
                long len = jedis.llen(key);
                List<String> allValuesInRedis = jedis.lrange(key, 0, len);

                for (int i = 0; i < allValuesInRedis.size(); i++) {
                    if (firstValue.equals(allValuesInRedis.get(i))) {
                        jedis.lset(key, i, secondValue);
                        break switchLabel;
                    }
                }
                jedis.rpush(key, secondValue);
                break;
        }

        jedis.close();
        next.accept(true);
    }
}
