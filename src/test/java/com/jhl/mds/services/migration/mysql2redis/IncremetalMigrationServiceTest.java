package com.jhl.mds.services.migration.mysql2redis;

import com.jhl.mds.BaseTest;
import com.jhl.mds.consts.RedisKeyType;
import com.jhl.mds.dto.SimpleFieldMappingDTO;
import com.jhl.mds.dto.TableInfoDTO;
import com.jhl.mds.dto.migration.MySQL2RedisMigrationDTO;
import org.junit.After;
import org.junit.Before;
import redis.clients.jedis.Jedis;

import java.util.Arrays;

public abstract class IncremetalMigrationServiceTest extends BaseTest {

    protected Jedis jedis;

    @Before
    public void before() {
        jedis = redisConnectionPool.getConnection(getRedisServerDTO());
    }

    @After
    public void after() {
        jedis.flushAll();
        jedis.close();
    }

    protected MySQL2RedisMigrationDTO.MySQL2RedisMigrationDTOBuilder getMigrationDTOBuilder(String sourceTable, String keyMapping, RedisKeyType keyType) {
        return MySQL2RedisMigrationDTO.builder()
                .taskId(randomTaskId())
                .source(new TableInfoDTO(getSourceMySQLServerDTO(), "mds", sourceTable))
                .target(getRedisServerDTO())
                .redisKeyType(keyType)
                .migrationActionCode(0b111)
                .mapping(Arrays.asList(
                        new SimpleFieldMappingDTO(keyMapping, "key"),
                        new SimpleFieldMappingDTO("json(_row)", "value")
                ));
    }
}
