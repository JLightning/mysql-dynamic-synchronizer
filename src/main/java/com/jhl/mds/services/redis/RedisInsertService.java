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

    public RedisInsertService(
            RedisConnectionPool redisConnectionPool,
            MySQLEventPrimaryKeyLock mySQLEventPrimaryKeyLock
    ) {
        this.redisConnectionPool = redisConnectionPool;
        this.mySQLEventPrimaryKeyLock = mySQLEventPrimaryKeyLock;
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
                        Long len = jedis.llen(key);
                        if (len == null || len == 0) {
                            jedis.rpush(key, value);
                        } else {
                            Object insertSortKey = getSortKey(value, context.getSortBy());
                            int idx = binarySearch(jedis, context.getSortBy(), key, len.intValue(), insertSortKey);
                            if (idx < 0) {
                                jedis.lpush(key, value);
                            } else if (idx >= len) {
                                jedis.rpush(key, value);
                            } else {
                                String insertAfter = jedis.lindex(key, idx);
                                jedis.linsert(key, BinaryClient.LIST_POSITION.AFTER, insertAfter, value);
                            }
                        }
                    } finally {
                        mySQLEventPrimaryKeyLock.unlock(context, Collections.singletonList(lockKey));
                    }

                }
                break;
        }

        jedis.close();

        next.accept(true);
    }

    private int binarySearch(Jedis jedis, SortDTO sortBy, String key, int len, Object searchFor) throws IOException {
        int l = 0, r = len - 1;
        while (r >= l) {
            int mid = l + (r - l) / 2;
            Object midSortKey = getSortKey(jedis.lindex(key, mid), sortBy);
            if (compare(midSortKey, searchFor) == 0) {
                return mid;
            }

            if ((sortBy.getDirection() == SortDTO.Direction.DESC) ^ (compare(midSortKey, searchFor) > 0)) {
                r = mid - 1;
            } else {
                l = mid + 1;
            }
        }

        return r;
    }

    private int compare(Object a, Object b) {
        if (a instanceof Comparable) {
            return ((Comparable) a).compareTo(b);
        }
        throw new RuntimeException("cannot compare");
    }

    private Object getSortKey(String value, SortDTO sortBy) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> data = objectMapper.readValue(value, new TypeReference<Map<String, Object>>() {
        });
        return data.get(sortBy.getKey());
    }
}
