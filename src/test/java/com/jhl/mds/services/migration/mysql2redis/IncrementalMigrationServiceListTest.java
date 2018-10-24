package com.jhl.mds.services.migration.mysql2redis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jhl.mds.TableTemplate;
import com.jhl.mds.consts.RedisKeyType;
import com.jhl.mds.dto.SortDTO;
import com.jhl.mds.dto.migration.MySQL2RedisMigrationDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Slf4j
public class IncrementalMigrationServiceListTest extends IncremetalMigrationServiceTest {

    @Autowired
    private IncrementalMigrationService incrementalMigrationService;

    @Test
    public void insertTest() throws Exception {
        String sourceTable = prepareTable(TableTemplate.TEMPLATE_SIMPLE);

        String keyPrefix = "key_name_" + rand.nextInt(70000) + "_";

        MySQL2RedisMigrationDTO dto = getMigrationDTOBuilder(sourceTable, "'" + keyPrefix + "'", RedisKeyType.LIST)
                .sortBy(new SortDTO("id", SortDTO.Direction.DESC))
                .build();

        incrementalMigrationService.run(dto);

        Thread.sleep(500);

        for (int i = 0; i < 100; i++) {
            getStatement().execute("INSERT INTO mds." + sourceTable + "(`random_number`) VALUES (1)");
        }

        Thread.sleep(3000);

        Assert.assertEquals(100L, jedis.llen(keyPrefix).longValue());

        List<String> values = jedis.lrange(keyPrefix, 0, -1);
        int lastId = Integer.MAX_VALUE;
        for (String valueJson : values) {
            Map<String, Object> value = objectMapper.readValue(valueJson, new TypeReference<Map<String, Object>>() {
            });

            Integer id = Integer.valueOf(String.valueOf(value.get("id")));
            Assert.assertTrue(id < lastId);

            lastId = id;
        }

        incrementalMigrationService.stop(dto);
    }

    @Test
    public void updateTest() throws Exception {
        String sourceTable = prepareTable(TableTemplate.TEMPLATE_SIMPLE);

        String keyPrefix = "key_name_" + rand.nextInt(70000) + "_";

        MySQL2RedisMigrationDTO dto = getMigrationDTOBuilder(sourceTable, "'" + keyPrefix + "'", RedisKeyType.LIST)
                .sortBy(new SortDTO("random_number", SortDTO.Direction.DESC))
                .build();

        incrementalMigrationService.run(dto);

        Thread.sleep(500);

        for (int i = 0; i < 100; i++) {
            getStatement().execute("INSERT INTO mds." + sourceTable + "(`random_number`) VALUES (1)");
        }

        getStatement().execute("UPDATE mds." + sourceTable + " SET random_number = id");

        Thread.sleep(3000);

        Set<Integer> set = new HashSet<>();

        int lastRandomNumber = Integer.MAX_VALUE;
        for (String json : jedis.lrange(keyPrefix, 0, 100)) {
            Map<String, Integer> m = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });

            int id = m.get("id");
            int randomNumber = m.get("random_number");
            Assert.assertEquals(randomNumber, id);

            set.add(randomNumber);

            Assert.assertTrue(randomNumber < lastRandomNumber);
            lastRandomNumber = randomNumber;
        }

        Assert.assertEquals(100, set.size());

        incrementalMigrationService.stop(dto);
    }

    @Test
    public void updateWithFilterDeleteNeededTest() throws Exception {
        String sourceTable = prepareTable(TableTemplate.TEMPLATE_SIMPLE);

        String keyPrefix = "key_name_" + rand.nextInt(70000) + "_";

        MySQL2RedisMigrationDTO dto = getMigrationDTOBuilder(sourceTable, "'" + keyPrefix + "'", RedisKeyType.LIST)
                .filters(Collections.singletonList("random_number % 2 == 0"))
                .build();

        incrementalMigrationService.run(dto);

        Thread.sleep(500);

        for (int i = 0; i < 100; i++) {
            getStatement().execute(String.format("INSERT INTO mds." + sourceTable + "(`random_number`) VALUES (%d)", 2));
        }

        getStatement().execute("UPDATE mds." + sourceTable + " SET random_number = id");

        Thread.sleep(3000);

        Assert.assertEquals(50, jedis.llen(keyPrefix).longValue());

        Set<Integer> set = new HashSet<>();

        for (String json : jedis.lrange(keyPrefix, 0, 100)) {
            Map<String, Integer> m = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });

            Integer random_number = m.get("random_number");
            Assert.assertEquals(0, random_number % 2);

            set.add(random_number);
        }

        Assert.assertEquals(50, set.size());

        incrementalMigrationService.stop(dto);
    }

    @Test
    public void updateWithFilterInsertNeededTest() throws Exception {
        String sourceTable = prepareTable(TableTemplate.TEMPLATE_SIMPLE);

        String keyPrefix = "key_name_" + rand.nextInt(70000) + "_";

        MySQL2RedisMigrationDTO dto = getMigrationDTOBuilder(sourceTable, "'" + keyPrefix + "'", RedisKeyType.LIST)
                .filters(Collections.singletonList("random_number % 2 == 0"))
                .build();

        incrementalMigrationService.run(dto);

        Thread.sleep(500);

        for (int i = 0; i < 100; i++) {
            getStatement().execute(String.format("INSERT INTO mds." + sourceTable + "(`random_number`) VALUES (%d)", 3));
        }

        getStatement().execute("UPDATE mds." + sourceTable + " SET random_number = id");

        Thread.sleep(3000);

        Assert.assertEquals(50, jedis.llen(keyPrefix).longValue());

        Set<Integer> set = new HashSet<>();

        for (String json : jedis.lrange(keyPrefix, 0, 100)) {
            Map<String, Integer> m = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });

            Integer random_number = m.get("random_number");
            Assert.assertEquals(0, random_number % 2);

            set.add(random_number);
        }

        Assert.assertEquals(50, set.size());

        incrementalMigrationService.stop(dto);
    }

    @Test
    public void deleteTest() throws Exception {
        String sourceTable = prepareTable(TableTemplate.TEMPLATE_SIMPLE);

        String keyPrefix = "key_name_" + rand.nextInt(70000) + "_";

        MySQL2RedisMigrationDTO dto = getMigrationDTOBuilder(sourceTable, "'" + keyPrefix + "'", RedisKeyType.LIST).build();

        incrementalMigrationService.run(dto);

        Thread.sleep(500);

        for (int i = 0; i < 100; i++) {
            getStatement().execute("INSERT INTO mds." + sourceTable + "(`random_number`) VALUES (1)");
        }

        getStatement().execute("DELETE FROM mds." + sourceTable);

        Thread.sleep(2000);

        Assert.assertEquals(0L, jedis.llen(keyPrefix).longValue());

        incrementalMigrationService.stop(dto);
    }
}
