package com.jhl.mds.dto.migration;

import com.jhl.mds.consts.MigrationAction;
import com.jhl.mds.consts.MigrationType;
import com.jhl.mds.consts.RedisKeyType;
import com.jhl.mds.dto.RedisServerDTO;
import com.jhl.mds.dto.SimpleFieldMappingDTO;
import com.jhl.mds.dto.TableInfoDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MySQL2RedisMigrationDTO implements MySQLSourceMigrationDTO {

    private int taskId;
    private TableInfoDTO source;
    private RedisServerDTO target;
    private List<SimpleFieldMappingDTO> mapping;
    private List<String> filters;
    private RedisKeyType redisKeyType;
    private int migrationActionCode;
    private boolean sequential;
}
