package com.jhl.mds.services.mysql;

import com.jhl.mds.dto.MySQLFieldDTO;
import com.jhl.mds.dto.MySQLFieldWithMappingDTO;
import com.jhl.mds.dto.MySQLServerDTO;
import com.jhl.mds.dto.TableFieldsMappingDTO;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class MySQLService {

    private MySQLConnectionPool mySQLConnectionPool;

    public MySQLService(MySQLConnectionPool mySQLConnectionPool) {
        this.mySQLConnectionPool = mySQLConnectionPool;
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

    public List<MySQLFieldWithMappingDTO> getFieldsMappingFor2Table(MySQLServerDTO sourceServer, MySQLServerDTO targetServer, TableFieldsMappingDTO dto) throws SQLException {
        List<MySQLFieldDTO> sourceFields = getFields(sourceServer, dto.getSourceDatabase(), dto.getSourceTable());
        List<MySQLFieldDTO> targetFields = getFields(targetServer, dto.getTargetDatabase(), dto.getTargetTable());
        HashMap<MySQLFieldDTO, Boolean> mapAlready = new HashMap<>();

        List<MySQLFieldWithMappingDTO> result = new ArrayList<>();

        outer_loop:
        for (MySQLFieldDTO sourceField : sourceFields) {
            for (MySQLFieldDTO targetField : targetFields) {
                if (sourceField.getField().equals(targetField.getField()) && !mapAlready.containsKey(targetField)) {
                    result.add(MySQLFieldWithMappingDTO.builder().sourceField(sourceField).targetField(targetField).mappable(true).build());
                    mapAlready.put(targetField, true);
                    continue outer_loop;
                }
            }

            result.add(MySQLFieldWithMappingDTO.builder().sourceField(sourceField).build());
        }

        targetFields.removeAll(mapAlready.keySet());

        for (MySQLFieldWithMappingDTO _result : result) {
            if (targetFields.size() == 0) break;
            if (!_result.isMappable()) {
                _result.setTargetField(targetFields.get(0));
                targetFields.remove(0);
            }
        }

        for (MySQLFieldDTO targetField : targetFields) {
            result.add(MySQLFieldWithMappingDTO.builder().targetField(targetField).build());
        }
        return result;
    }
}
