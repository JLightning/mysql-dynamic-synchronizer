package com.jhl.mds.services.mysql;

import com.jhl.mds.dto.*;
import com.jhl.mds.services.customefilter.CustomFilterPool;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MySQLDescribeService {

    private MySQLConnectionPool mySQLConnectionPool;
    private CustomFilterPool customFilterPool;
    private MySQLFieldDefaultValueService mySQLFieldDefaultValueService;

    public MySQLDescribeService(
            MySQLConnectionPool mySQLConnectionPool,
            CustomFilterPool customFilterPool,
            MySQLFieldDefaultValueService mySQLFieldDefaultValueService
    ) {
        this.mySQLConnectionPool = mySQLConnectionPool;
        this.customFilterPool = customFilterPool;
        this.mySQLFieldDefaultValueService = mySQLFieldDefaultValueService;
    }

    public List<String> getDatabases(MySQLServerDTO dto) throws SQLException {
        Connection conn = mySQLConnectionPool.getConnection(dto);
        Statement st = conn.createStatement();

        ResultSet rs = st.executeQuery("SHOW DATABASES;");

        List<String> databaseNames = new ArrayList<>();
        while (rs.next()) {
            String tableName = rs.getString(1);
            databaseNames.add(tableName);
        }

        return databaseNames;
    }

    public List<String> getTables(MySQLServerDTO dto, String database) throws SQLException {
        Connection conn = mySQLConnectionPool.getConnection(dto);
        Statement st = conn.createStatement();

        st.executeQuery("USE " + database);
        ResultSet rs = st.executeQuery("SHOW TABLES;");

        List<String> tableNames = new ArrayList<>();
        while (rs.next()) {
            String tableName = rs.getString(1);
            tableNames.add(tableName);
        }
        return tableNames;
    }

    public List<MySQLFieldDTO> getFields(MySQLServerDTO dto, String database, String table) throws SQLException {
        Connection conn = mySQLConnectionPool.getConnection(dto);
        Statement st = conn.createStatement();

        st.executeQuery("USE " + database);
        ResultSet rs = st.executeQuery("DESCRIBE " + table + ";");

        List<MySQLFieldDTO> fields = new ArrayList<>();
        while (rs.next()) {
            fields.add(MySQLFieldDTO.builder()
                    .field(rs.getString(1))
                    .type(rs.getString(2))
                    .nullable(!rs.getString(3).equals("NO"))
                    .key(rs.getString(4))
                    .defaultValue(rs.getString(5))
                    .extra(rs.getString(6))
                    .build());
        }
        return fields;
    }

    public List<MySQLFieldWithMappingDTO> getFieldsMappingFor2Table(MySQLServerDTO sourceServer, MySQLServerDTO targetServer, TableFieldsMappingRequestDTO dto) throws SQLException {
        List<MySQLFieldDTO> sourceFields = getFields(sourceServer, dto.getSourceDatabase(), dto.getSourceTable());
        List<MySQLFieldDTO> targetFields = getFields(targetServer, dto.getTargetDatabase(), dto.getTargetTable());

        List<MySQLFieldWithMappingDTO> result = new ArrayList<>();

        HashMap<MySQLFieldDTO, Boolean> mapAlreadyForTarget = new HashMap<>();

        Map<String, String> sourceToTargetMap = null;
        if (dto.getMapping() != null) {
            sourceToTargetMap = dto.getMapping().stream().collect(Collectors.toMap(SimpleFieldMappingDTO::getSourceField, SimpleFieldMappingDTO::getTargetField));
        }

        outer_loop:
        for (MySQLFieldDTO sourceField : sourceFields) {
            for (MySQLFieldDTO targetField : targetFields) {
                boolean shouldMap = sourceField.getField().equals(targetField.getField());
                if (sourceToTargetMap != null)
                    shouldMap = targetField.getField().equals(sourceToTargetMap.get(sourceField.getField()));

                if (shouldMap && !mapAlreadyForTarget.containsKey(targetField)) {
                    result.add(MySQLFieldWithMappingDTO.builder().sourceField(sourceField.getField()).targetField(targetField.getField()).mappable(true).build());
                    mapAlreadyForTarget.put(targetField, true);
                    continue outer_loop;
                }
            }

            result.add(MySQLFieldWithMappingDTO.builder().sourceField(sourceField.getField()).build());
        }

        targetFields.removeAll(mapAlreadyForTarget.keySet());

        for (MySQLFieldWithMappingDTO _result : result) {
            if (targetFields.size() == 0) break;
            if (!_result.isMappable()) {
                _result.setTargetField(targetFields.get(0).getField());
                targetFields.remove(0);
            }
        }

        for (MySQLFieldDTO targetField : targetFields) {
            result.add(MySQLFieldWithMappingDTO.builder().targetField(targetField.getField()).build());
        }
        return result;
    }

    public void validateFilter(MySQLServerDTO dto, String database, String table, String filter) throws Exception {
        filter = filter.replaceAll("&", "&&");
        filter = filter.replaceAll("\\|", "||");

        List<MySQLFieldDTO> fields = getFields(dto, database, table);
        Map<String, Object> sampleData = fields.stream().map(field -> new Object[]{field, mySQLFieldDefaultValueService.getDefaultValue(field)}).collect(Collectors.toMap(o -> ((MySQLFieldDTO) o[0]).getField(), o -> (String) o[1]));

        customFilterPool.resolve(filter, sampleData).get();
    }

    public String beautifyFilter(String filter) {
        filter = filter.replaceAll("&", "&&");
        filter = filter.replaceAll("\\|", "||");
        String allOperatersRegex = "(\\+|-|\\*|/|>|>=|<|<=|&&|\\|\\|)";
        filter = filter.replaceAll(" {2,}", " ");
        filter = filter.replaceAll("([^ ])" + allOperatersRegex + "([^ ])", "$1 $2 $3");
        filter = filter.replaceAll("([^ ])" + allOperatersRegex, "$1 $2");
        filter = filter.replaceAll(allOperatersRegex+ "([^ ])", "$1 $2");
        filter = filter.replaceAll("\\( \\(", "((");
        filter = filter.replaceAll("\\) \\)", "))");
        return filter;
    }
}
