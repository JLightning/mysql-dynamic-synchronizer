package com.jhl.mds.services.migration.mysql2redis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhl.mds.TableTemplate;
import com.jhl.mds.consts.RedisKeyType;
import com.jhl.mds.dto.migration.MySQL2RedisMigrationDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.Map;
import java.util.Set;

@Slf4j
public class IncrementalMigrationServiceStringTest extends IncremetalMigrationServiceTest {

    @Autowired
    private IncrementalMigrationService incrementalMigrationService;

    @Test
    public void insertTest() throws Exception {
        String sourceTable = prepareTable(TableTemplate.TEMPLATE_SIMPLE);

        String keyPrefix = "key_name_" + rand.nextInt(70000) + "_";

        MySQL2RedisMigrationDTO dto = getMigrationDTOBuilder(sourceTable, "'" + keyPrefix + "' + id", RedisKeyType.STRING).build();

        incrementalMigrationService.run(dto);

        Thread.sleep(500);

        for (int i = 0; i < 100; i++) {
            getStatement().execute("INSERT INTO mds." + sourceTable + "(`random_number`) VALUES (1)");
        }

        Thread.sleep(3000);

        Jedis jedis = redisConnectionPool.getConnection(getRedisServerDTO());

        Set<String> keys = jedis.keys(keyPrefix + "*");

        Assert.assertEquals(100, keys.size());

        jedis.flushAll();

        incrementalMigrationService.stop(dto);
    }

    @Test
    public void updateTest() throws Exception {
        String sourceTable = prepareTable(TableTemplate.TEMPLATE_SIMPLE);

        String keyPrefix = "key_name_" + rand.nextInt(70000) + "_";

        MySQL2RedisMigrationDTO dto = getMigrationDTOBuilder(sourceTable, "'" + keyPrefix + "' + id", RedisKeyType.STRING).build();

        incrementalMigrationService.run(dto);

        Thread.sleep(500);

        for (int i = 0; i < 100; i++) {
            getStatement().execute("INSERT INTO mds." + sourceTable + "(`random_number`) VALUES (1)");
        }

        getStatement().execute("UPDATE mds." + sourceTable + " SET random_number = 2");

        Thread.sleep(3000);

        Jedis jedis = redisConnectionPool.getConnection(getRedisServerDTO());

        Set<String> keys = jedis.keys(keyPrefix + "*");

        Assert.assertEquals(100, keys.size());

        ObjectMapper objectMapper = new ObjectMapper();

        for (String key : keys) {
            String value = jedis.get(key);
            Map<String, Object> m = objectMapper.readValue(value, new TypeReference<Map<String, Object>>() {
            });
            Assert.assertEquals(2, m.get("random_number"));
        }

        jedis.flushAll();

        incrementalMigrationService.stop(dto);
    }

    @Test
    public void deleteTest() throws Exception {
        String sourceTable = prepareTable(TableTemplate.TEMPLATE_SIMPLE);

        String keyPrefix = "key_name_" + rand.nextInt(70000) + "_";

        MySQL2RedisMigrationDTO dto = getMigrationDTOBuilder(sourceTable, "'" + keyPrefix + "' + id", RedisKeyType.STRING).build();

        incrementalMigrationService.run(dto);

        Thread.sleep(500);

        for (int i = 0; i < 100; i++) {
            getStatement().execute("INSERT INTO mds." + sourceTable + "(`random_number`) VALUES (1)");
        }

        getStatement().execute("DELETE FROM mds." + sourceTable);

        Thread.sleep(2000);

        Jedis jedis = redisConnectionPool.getConnection(getRedisServerDTO());

        Set<String> keys = jedis.keys(keyPrefix + "*");

        Assert.assertEquals(0, keys.size());

        incrementalMigrationService.stop(dto);
    }
}
