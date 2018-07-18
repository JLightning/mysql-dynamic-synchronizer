package com.jhl.mds.services.migration.mysql2mysql;

import com.jhl.mds.BaseTest;
import com.jhl.mds.dto.FullMigrationDTO;
import com.jhl.mds.dto.SimpleFieldMappingDTO;
import com.jhl.mds.dto.TableInfoDTO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

public class StructureMigrationServiceTest extends BaseTest {

    @Autowired
    private StrutureMigrationService strutureMigrationService;

    @Test
    public void executeTest() throws Exception {
        getStatement().execute("USE mds");
        getStatement().execute("DROP TABLE IF EXISTS task_tmp");

        FullMigrationDTO dto = FullMigrationDTO.builder()
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

        strutureMigrationService.execute(dto, null, null, null);
    }
}
