package com.jhl.mds.services.redis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhl.mds.dto.RedisServerDTO;
import com.jhl.mds.dto.SortDTO;
import org.springframework.stereotype.Service;
import redis.clients.jedis.BinaryClient;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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

    public void insertSorted(Jedis jedis, SortDTO sortBy, String key, String value) throws IOException {
        Long len = jedis.llen(key);
        if (len == null || len == 0) {
            jedis.rpush(key, value);
        } else {
            Object insertSortKey = getSortKey(value, sortBy);
            int idx = binarySearch(jedis, sortBy, key, len.intValue(), insertSortKey);
            if (idx < 0) {
                jedis.lpush(key, value);
            } else if (idx >= len) {
                jedis.rpush(key, value);
            } else {
                String insertAfter = jedis.lindex(key, idx);
                jedis.linsert(key, BinaryClient.LIST_POSITION.AFTER, insertAfter, value);
            }
        }
    }

    private int binarySearch(Jedis jedis, SortDTO sortBy, String key, int len, Object searchFor) throws IOException {
        int l = 0, r = len - 1;
        while (r >= l) {
            int mid = l + (r - l) / 2;
            Object midSortKey = getSortKey(jedis.lindex(key, mid), sortBy);
            if (compare(midSortKey, searchFor) == 0) {
                return mid;
            }

            if ((SortDTO.Direction.DESC == sortBy.getDirection()) ^ (compare(midSortKey, searchFor) > 0)) {
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
