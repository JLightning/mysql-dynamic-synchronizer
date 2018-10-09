package com.jhl.mds.services.migration.mysql2redis;

import com.jhl.mds.BaseTest;
import com.jhl.mds.TableTemplate;
import com.jhl.mds.dto.MySQLServerDTO;
import com.jhl.mds.dto.RedisServerDTO;
import com.jhl.mds.dto.SimpleFieldMappingDTO;
import com.jhl.mds.dto.TableInfoDTO;
import com.jhl.mds.dto.migration.MySQL2RedisMigrationDTO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

public class IncrementalMigrationServiceTest extends BaseTest {

    @Autowired
    private IncrementalMigrationService incrementalMigrationService;

    @Test
    public void insertTest() throws Exception {
        String sourceTable = prepareTable(TableTemplate.TEMPLATE_SIMPLE);

        MySQLServerDTO serverDTO = new MySQLServerDTO(0, "test", "localhost", "3307", "root", "root");

        MySQL2RedisMigrationDTO dto = MySQL2RedisMigrationDTO.builder()
                .taskId((int) (Math.random() * 10000))
                .source(new TableInfoDTO(serverDTO, "mds", sourceTable))
                .target(new RedisServerDTO(0, "", "localhost", "6379", "", ""))
                .mapping(Arrays.asList(
                        new SimpleFieldMappingDTO("'key_name_' + id", "key"),
                        new SimpleFieldMappingDTO("_row", "value")
                ))
                .build();

        incrementalMigrationService.run(dto);

        Thread.sleep(500);

        for (int i = 0; i < 100; i++) {
            getStatement().execute("INSERT INTO mds." + sourceTable + "(`random_number`) VALUES (1)");
        }

        Thread.sleep(2000);
    }
}
