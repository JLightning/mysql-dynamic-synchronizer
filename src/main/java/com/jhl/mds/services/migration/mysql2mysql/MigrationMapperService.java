package com.jhl.mds.services.migration.mysql2mysql;

import com.jhl.mds.dto.FullMigrationDTO;
import com.jhl.mds.dto.MySQLFieldDTO;
import com.jhl.mds.dto.SimpleFieldMappingDTO;
import com.jhl.mds.dto.TableInfoDTO;
import com.jhl.mds.services.custommapping.CustomMappingPool;
import com.jhl.mds.services.mysql.MySQLDescribeService;
import com.jhl.mds.services.mysql.MySQLFieldDefaultValueService;
import com.jhl.mds.util.MySQLStringUtil;
import com.jhl.mds.util.PipeLineTaskRunner;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MigrationMapperService implements PipeLineTaskRunner<FullMigrationDTO, Map<String, Object>, String> {

    private final List<MySQLFieldDTO> targetFields;
    private final Map<String, MySQLFieldDTO> targetFieldMap;
    @Getter
    private final List<String> columns;
    private MySQLFieldDefaultValueService mySQLFieldDefaultValueService;
    private List<SimpleFieldMappingDTO> mapping;
    private CustomMappingPool customMapping;

    public MigrationMapperService(
            MySQLDescribeService mySQLDescribeService,
            MySQLFieldDefaultValueService mySQLFieldDefaultValueService,
            CustomMappingPool customMapping,
            TableInfoDTO tableInfo,
            List<SimpleFieldMappingDTO> mapping
    ) throws SQLException {
        this.mySQLFieldDefaultValueService = mySQLFieldDefaultValueService;
        this.mapping = mapping;
        this.customMapping = customMapping;
        targetFields = mySQLDescribeService.getFields(tableInfo.getServer(), tableInfo.getDatabase(), tableInfo.getTable());
        targetFieldMap = targetFields.stream().collect(Collectors.toMap(MySQLFieldDTO::getField, o -> o));
        columns = targetFields.stream().map(MySQLFieldDTO::getField).collect(Collectors.toList());
    }

    public Map<String, Object> map(Map<String, Object> data) {
        Map<String, String> targetToSourceColumnMatch = mapping.stream().collect(Collectors.toMap(SimpleFieldMappingDTO::getTargetField, SimpleFieldMappingDTO::getSourceField));

        Map<String, Object> mappedData = new LinkedHashMap<>();

        for (String targetColumn : columns) {
            if (targetToSourceColumnMatch.containsKey(targetColumn)) {
                String sourceColumn = targetToSourceColumnMatch.get(targetColumn);
                if (data.containsKey(sourceColumn)) {
                    mappedData.put(targetColumn, data.get(sourceColumn));
                } else {
                    try {
                        mappedData.put(targetColumn, customMapping.resolve(sourceColumn, data).get());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                mappedData.put(targetColumn, mySQLFieldDefaultValueService.getDefaultValue(targetFieldMap.get(targetColumn)));
            }
        }

        return mappedData;
    }

    public String mapToString(Map<String, Object> data) {
        return MySQLStringUtil.valueListString(map(data).values());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void queue(FullMigrationDTO context, Map<String, Object> input, Consumer<String> next) {
        next.accept(mapToString(input));
    }

    @Service
    public static class Factory {

        private MySQLDescribeService mySQLDescribeService;
        private MySQLFieldDefaultValueService mySQLFieldDefaultValueService;
        private CustomMappingPool customMapping;

        @Autowired
        public Factory(MySQLDescribeService mySQLDescribeService, MySQLFieldDefaultValueService mySQLFieldDefaultValueService, CustomMappingPool customMapping) {
            this.mySQLDescribeService = mySQLDescribeService;
            this.mySQLFieldDefaultValueService = mySQLFieldDefaultValueService;
            this.customMapping = customMapping;
        }

        public MigrationMapperService create(TableInfoDTO tableInfo, List<SimpleFieldMappingDTO> mapping) throws SQLException {
            return new MigrationMapperService(mySQLDescribeService, mySQLFieldDefaultValueService, customMapping, tableInfo, mapping);
        }
    }
}
