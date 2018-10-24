package com.jhl.mds.services.redis;

import com.jhl.mds.dto.PairOfMap;
import com.jhl.mds.dto.migration.MySQL2RedisMigrationDTO;
import com.jhl.mds.services.mysql.MySQLEventPrimaryKeyLock;
import com.jhl.mds.util.pipeline.PipeLineTaskRunner;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

@Service
public class RedisUpdateService implements PipeLineTaskRunner<MySQL2RedisMigrationDTO, PairOfMap, Boolean> {

    private RedisConnectionPool redisConnectionPool;
    private MySQLEventPrimaryKeyLock mySQLEventPrimaryKeyLock;
    private RedisListUtil redisListUtil;

    public RedisUpdateService(
            RedisConnectionPool redisConnectionPool,
            MySQLEventPrimaryKeyLock mySQLEventPrimaryKeyLock,
            RedisListUtil redisListUtil
    ) {
        this.redisConnectionPool = redisConnectionPool;
        this.mySQLEventPrimaryKeyLock = mySQLEventPrimaryKeyLock;
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

                jedis.lrem(key, 0, firstValue);
                if (context.getSortBy() == null) {
                    jedis.rpush(key, secondValue);
                } else {
                    Object lockKey = mySQLEventPrimaryKeyLock.lock(context, key);
                    try {
                        redisListUtil.insertSorted(jedis, context.getSortBy(), key, secondValue);
                    } finally {
                        mySQLEventPrimaryKeyLock.unlock(context, Collections.singletonList(lockKey));
                    }

                }
                break;
        }

        jedis.close();
        next.accept(true);
    }
}
