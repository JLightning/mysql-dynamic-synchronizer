package com.jhl.mds.services.migration;

import com.jhl.mds.dto.MySQLFieldDTO;
import com.jhl.mds.dto.MySQLServerDTO;
import com.jhl.mds.dto.TaskDTO;
import com.jhl.mds.services.mysql.MySQLDescribeService;
import com.jhl.mds.services.mysql.MySQLFieldDefaultValueService;
import com.jhl.mds.util.MySQLStringUtil;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MySQL2MySQLMigrationMapperService {

    private final List<MySQLFieldDTO> targetFields;
    private final Map<String, MySQLFieldDTO> targetFieldMap;
    @Getter
    private final List<String> columns;
    private MySQLFieldDefaultValueService mySQLFieldDefaultValueService;
    private List<TaskDTO.Mapping> mapping;

    public MySQL2MySQLMigrationMapperService(
            MySQLDescribeService mySQLDescribeService,
            MySQLFieldDefaultValueService mySQLFieldDefaultValueService,
            MySQLServerDTO serverDTO,
            TaskDTO.Table tableInfo,
            List<TaskDTO.Mapping> mapping
    ) throws SQLException {
        this.mySQLFieldDefaultValueService = mySQLFieldDefaultValueService;
        this.mapping = mapping;
        targetFields = mySQLDescribeService.getFields(serverDTO, tableInfo.getDatabase(), tableInfo.getTable());
        targetFieldMap = targetFields.stream().collect(Collectors.toMap(MySQLFieldDTO::getField, o -> o));
        columns = targetFields.stream().map(MySQLFieldDTO::getField).collect(Collectors.toList());
    }

    public Map<String, Object> map(Map<String, Object> data) {
        Map<String, String> targetToSourceColumnMatch = mapping.stream().collect(Collectors.toMap(TaskDTO.Mapping::getTargetField, TaskDTO.Mapping::getSourceField));

        Map<String, Object> mappedData = new LinkedHashMap<>();

        for (String targetColumn : columns) {
            if (targetToSourceColumnMatch.containsKey(targetColumn)) {
                mappedData.put(targetColumn, data.get(targetToSourceColumnMatch.get(targetColumn)));
            } else {
                mappedData.put(targetColumn, mySQLFieldDefaultValueService.getDefaultValue(targetFieldMap.get(targetColumn)));
            }
        }

        return mappedData;
    }

    public String mapToString(Map<String, Object> data) {
        return MySQLStringUtil.valueListString(map(data).values());
    }

    @Service
    public static class Factory {

        private MySQLDescribeService mySQLDescribeService;
        private MySQLFieldDefaultValueService mySQLFieldDefaultValueService;

        @Autowired
        public Factory(MySQLDescribeService mySQLDescribeService, MySQLFieldDefaultValueService mySQLFieldDefaultValueService) {
            this.mySQLDescribeService = mySQLDescribeService;
            this.mySQLFieldDefaultValueService = mySQLFieldDefaultValueService;
        }

        public MySQL2MySQLMigrationMapperService create(MySQLServerDTO serverDTO, TaskDTO.Table tableInfo, List<TaskDTO.Mapping> mapping) throws SQLException {
            return new MySQL2MySQLMigrationMapperService(mySQLDescribeService, mySQLFieldDefaultValueService, serverDTO, tableInfo, mapping);
        }
    }
}
