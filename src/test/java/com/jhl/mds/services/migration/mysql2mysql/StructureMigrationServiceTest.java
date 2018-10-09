package com.jhl.mds.services.migration.mysql2mysql;

import com.jhl.mds.BaseTest;
import com.jhl.mds.TableTemplate;
import com.jhl.mds.dto.SimpleFieldMappingDTO;
import com.jhl.mds.dto.TableInfoDTO;
import com.jhl.mds.dto.migration.MigrationDTO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

public class StructureMigrationServiceTest extends BaseTest {

    @Autowired
    private StructureMigrationService structureMigrationService;

    @Test
    public void executeTest() throws Exception {
        String sourceTable = prepareTable(TableTemplate.TEMPLATE_SIMPLE);
        String targetTable = generateRandomTableName();

        getStatement().execute("USE mds");
        getStatement().execute("DROP TABLE IF EXISTS " + targetTable);

        MigrationDTO dto = MigrationDTO.builder()
                .source(new TableInfoDTO(getSourceServerDTO(), TEST_DATABASE, sourceTable))
                .target(new TableInfoDTO(getSourceServerDTO(), TEST_DATABASE, targetTable))
                .mapping(Arrays.asList(
                        new SimpleFieldMappingDTO("id", "id"),
                        new SimpleFieldMappingDTO("random_number", "random_number")
                ))
                .build();

        structureMigrationService.execute(dto, null, null, null);
    }
}
