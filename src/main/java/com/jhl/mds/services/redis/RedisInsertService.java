package com.jhl.mds.services.redis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhl.mds.dto.SortDTO;
import com.jhl.mds.dto.migration.MySQL2RedisMigrationDTO;
import com.jhl.mds.services.mysql.MySQLEventPrimaryKeyLock;
import com.jhl.mds.util.pipeline.PipeLineTaskRunner;
import org.springframework.stereotype.Service;
import redis.clients.jedis.BinaryClient;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

@Service
public class RedisInsertService implements PipeLineTaskRunner<MySQL2RedisMigrationDTO, Map<String, Object>, Boolean> {

    private RedisConnectionPool redisConnectionPool;
    private MySQLEventPrimaryKeyLock mySQLEventPrimaryKeyLock;
    private RedisListUtil redisListUtil;

    public RedisInsertService(
            RedisConnectionPool redisConnectionPool,
            MySQLEventPrimaryKeyLock mySQLEventPrimaryKeyLock,
            RedisListUtil redisListUtil
    ) {
        this.redisConnectionPool = redisConnectionPool;
        this.mySQLEventPrimaryKeyLock = mySQLEventPrimaryKeyLock;
        this.redisListUtil = redisListUtil;
    }

    @Override
    public void execute(MySQL2RedisMigrationDTO context, Map<String, Object> input, Consumer<Boolean> next, Consumer<Exception> errorHandler) throws Exception {
        Jedis jedis = redisConnectionPool.getConnection(context.getTarget());

        switch (context.getRedisKeyType()) {
            case STRING:
                jedis.set(String.valueOf(input.get("key")), String.valueOf(input.get("value")));
                break;
            case LIST:
                String key = String.valueOf(input.get("key"));
                String value = String.valueOf(input.get("value"));
                if (context.getSortBy() == null) {
                    jedis.rpush(key, value);
                } else {
                    Object lockKey = mySQLEventPrimaryKeyLock.lock(context, key);
                    try {
                        redisListUtil.insertSorted(jedis, context.getSortBy(), key, value);
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
