package com.jhl.mds.services.migration.mysql2mysql;

import com.jhl.mds.BaseTest;
import com.jhl.mds.TableTemplate;
import com.jhl.mds.dto.migration.MigrationDTO;
import com.jhl.mds.dto.SimpleFieldMappingDTO;
import com.jhl.mds.dto.TableInfoDTO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;

public class StructureMigrationServiceTest extends BaseTest {

    @Autowired
    private StructureMigrationService structureMigrationService;

    public void executeTest() throws Exception {
        prepareTable(TableTemplate.TEMPLATE_SIMPLE);

        getStatement().execute("USE mds");
        getStatement().execute("DROP TABLE IF EXISTS task_tmp");

        MigrationDTO dto = MigrationDTO.builder()
                .source(new TableInfoDTO(getSourceServerDTO(), "mds", "task"))
                .target(new TableInfoDTO(getSourceServerDTO(), "mds", "task_tmp"))
                .mapping(Arrays.asList(
                        new SimpleFieldMappingDTO("task_id", "task_tmp_id"),
                        new SimpleFieldMappingDTO("task_name", "task_tmp_name"),
                        new SimpleFieldMappingDTO("task_code", "task_tmp_code_xxx"),
                        new SimpleFieldMappingDTO("created_at", "created_at"),
                        new SimpleFieldMappingDTO("updated_at", "updated_at")
                ))
                .build();

        structureMigrationService.execute(dto, null, null, null);
    }
}
