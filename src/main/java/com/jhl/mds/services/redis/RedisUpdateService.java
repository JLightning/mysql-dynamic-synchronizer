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
    private RedisListUtil redisListUtil;

    public RedisUpdateService(
            RedisConnectionPool redisConnectionPool,
            RedisListUtil redisListUtil
    ) {
        super();
        this.redisConnectionPool = redisConnectionPool;
        this.redisListUtil = redisListUtil;
    }

    @Override
    public void execute(MySQL2RedisMigrationDTO context, PairOfMap input, Consumer<Boolean> next, Consumer<Exception> errorHandler) throws Exception {
        Jedis jedis = redisConnectionPool.getConnection(context.getTarget());

        Map<String, Object> first = input.getFirst();
        Map<String, Object> second = input.getSecond();

        switch (context.getRedisKeyType()) {
            case STRING:
                jedis.set(String.valueOf(second.get("key")), String.valueOf(second.get("value")));
                break;
            case LIST:
                String key = String.valueOf(first.get("key"));
                String firstValue = String.valueOf(first.get("value"));
                String secondValue = String.valueOf(second.get("value"));

                long idx = redisListUtil.findValueInList(context.getTarget(), key, firstValue);
                if (idx == -1) {
                    jedis.rpush(key, secondValue);
                } else {
                    jedis.lset(key, idx, secondValue);
                }
                break;
        }

        jedis.close();
        next.accept(true);
    }
}
