package com.jhl.mds.services.migration.mysql2mysql;

import com.jhl.mds.dto.MySQLFieldDTO;
import com.jhl.mds.dto.SimpleFieldMappingDTO;
import com.jhl.mds.dto.TableInfoDTO;
import com.jhl.mds.services.custommapping.CustomMappingPool;
import com.jhl.mds.services.mysql.MySQLDescribeService;
import com.jhl.mds.services.mysql.MySQLFieldDefaultValueService;
import com.jhl.mds.util.MySQLStringUtil;
import com.jhl.mds.util.pipeline.PipeLineTaskRunner;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MigrationMapperService implements PipeLineTaskRunner<Object, Map<String, Object>, String> {

    @Nullable
    private final Map<String, MySQLFieldDTO> targetFieldMap;
    @Getter
    private final List<String> columns;
    private MySQLFieldDefaultValueService mySQLFieldDefaultValueService;
    private List<SimpleFieldMappingDTO> mapping;
    private CustomMappingPool customMapping;

    public MigrationMapperService(
            MySQLFieldDefaultValueService mySQLFieldDefaultValueService,
            CustomMappingPool customMapping,
            List<SimpleFieldMappingDTO> mapping,
            Map<String, MySQLFieldDTO> targetFieldMap,
            List<String> columns) {
        this.mySQLFieldDefaultValueService = mySQLFieldDefaultValueService;
        this.mapping = mapping;
        this.customMapping = customMapping;
        this.targetFieldMap = targetFieldMap;
        this.columns = columns;
    }

    public Map<String, Object> map(Map<String, Object> data, boolean includeDefault) throws Exception {
        Map<String, String> targetToSourceColumnMatch = mapping.stream().collect(Collectors.toMap(SimpleFieldMappingDTO::getTargetField, SimpleFieldMappingDTO::getSourceField));

        Map<String, Object> mappedData = new LinkedHashMap<>();

        for (String targetColumn : columns) {
            Object value = null;
            if (targetToSourceColumnMatch.containsKey(targetColumn)) {
                String sourceColumn = targetToSourceColumnMatch.get(targetColumn);
                if (data.containsKey(sourceColumn)) {
                    value = data.get(sourceColumn);
                } else {
                    value = customMapping.resolve(sourceColumn, data).get();
                }
            } else if (targetFieldMap != null && !targetFieldMap.get(targetColumn).isNullable() && includeDefault) {
                value = mySQLFieldDefaultValueService.getDefaultValue(targetFieldMap.get(targetColumn));
            }
            if (value != null || includeDefault) {
                mappedData.put(targetColumn, value);
            }
        }

        return mappedData;
    }

    public String mapToString(Map<String, Object> data) throws Exception {
        return MySQLStringUtil.valueListString(map(data, true).values());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void execute(Object context, Map<String, Object> input, Consumer<String> next, Consumer<Exception> errorHandler) throws Exception {
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
            List<MySQLFieldDTO> targetFields = mySQLDescribeService.getFields(tableInfo.getServer(), tableInfo.getDatabase(), tableInfo.getTable());
            Map<String, MySQLFieldDTO> targetFieldMap = targetFields.stream().collect(Collectors.toMap(MySQLFieldDTO::getField, o -> o));
            List<String> columns = targetFields.stream().map(MySQLFieldDTO::getField).collect(Collectors.toList());

            return new MigrationMapperService(mySQLFieldDefaultValueService, customMapping, mapping, targetFieldMap, columns);
        }

        public MigrationMapperService create(List<SimpleFieldMappingDTO> mapping) {
            List<String> columns = mapping.stream().map(SimpleFieldMappingDTO::getTargetField).collect(Collectors.toList());
            return new MigrationMapperService(mySQLFieldDefaultValueService, customMapping, mapping, null, columns);
        }
    }
}
