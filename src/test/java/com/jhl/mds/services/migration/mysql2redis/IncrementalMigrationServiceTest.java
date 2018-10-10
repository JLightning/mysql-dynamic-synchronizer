package com.jhl.mds.services.migration.mysql2redis;

import com.jhl.mds.BaseTest;
import com.jhl.mds.TableTemplate;
import com.jhl.mds.dto.MySQLServerDTO;
import com.jhl.mds.dto.RedisServerDTO;
import com.jhl.mds.dto.SimpleFieldMappingDTO;
import com.jhl.mds.dto.TableInfoDTO;
import com.jhl.mds.dto.migration.MySQL2RedisMigrationDTO;
import com.jhl.mds.services.redis.RedisConnectionPool;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.Arrays;
import java.util.Random;
import java.util.Set;

@Slf4j
public class IncrementalMigrationServiceTest extends BaseTest {

    @Autowired
    private IncrementalMigrationService incrementalMigrationService;

    @Autowired
    private RedisConnectionPool redisConnectionPool;

    Random rand = new Random();

    @Test
    public void insertTest() throws Exception {
        String sourceTable = prepareTable(TableTemplate.TEMPLATE_SIMPLE);

        MySQLServerDTO serverDTO = new MySQLServerDTO(0, "test", "localhost", "3307", "root", "root");

        String keyPrefix = "key_name_" + rand.nextInt(70000) + "_";

        RedisServerDTO redisServerDTO = new RedisServerDTO(0, "", "localhost", "6379", "", "");
        MySQL2RedisMigrationDTO dto = MySQL2RedisMigrationDTO.builder()
                .taskId((int) (Math.random() * 10000))
                .source(new TableInfoDTO(serverDTO, "mds", sourceTable))
                .target(redisServerDTO)
                .mapping(Arrays.asList(
                        new SimpleFieldMappingDTO("'" + keyPrefix + "' + id", "key"),
                        new SimpleFieldMappingDTO("json(_row)", "value")
                ))
                .build();

        incrementalMigrationService.run(dto);

        Thread.sleep(500);

        for (int i = 0; i < 100; i++) {
            getStatement().execute("INSERT INTO mds." + sourceTable + "(`random_number`) VALUES (1)");
        }

        Thread.sleep(3000);

        Jedis jedis = redisConnectionPool.getConnection(redisServerDTO);
        Set<String> keys = jedis.keys(keyPrefix + "*");

        Assert.assertEquals(100, keys.size());

        jedis.flushAll();
    }

    @Test
    public void updateTest() throws Exception {
        String sourceTable = prepareTable(TableTemplate.TEMPLATE_SIMPLE);

        MySQLServerDTO serverDTO = new MySQLServerDTO(0, "test", "localhost", "3307", "root", "root");

        String keyPrefix = "key_name_" + rand.nextInt(70000) + "_";

        RedisServerDTO redisServerDTO = new RedisServerDTO(0, "", "localhost", "6379", "", "");
        MySQL2RedisMigrationDTO dto = MySQL2RedisMigrationDTO.builder()
                .taskId((int) (Math.random() * 10000))
                .source(new TableInfoDTO(serverDTO, "mds", sourceTable))
                .target(redisServerDTO)
                .mapping(Arrays.asList(
                        new SimpleFieldMappingDTO("'" + keyPrefix + "' + id", "key"),
                        new SimpleFieldMappingDTO("json(_row)", "value")
                ))
                .build();

        incrementalMigrationService.run(dto);

        Thread.sleep(500);

        for (int i = 0; i < 100; i++) {
            getStatement().execute("INSERT INTO mds." + sourceTable + "(`random_number`) VALUES (1)");
        }

        Thread.sleep(500);

        getStatement().execute("UPDATE mds." + sourceTable + " SET random_number = 2");

        Thread.sleep(3000);

        Jedis jedis = redisConnectionPool.getConnection(redisServerDTO);
        Set<String> keys = jedis.keys(keyPrefix + "*");

        Assert.assertEquals(100, keys.size());

        jedis.flushAll();
    }
}
