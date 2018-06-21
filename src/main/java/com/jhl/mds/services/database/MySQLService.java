package com.jhl.mds.services.database;

import com.jhl.mds.dto.MysqlFieldDTO;
import com.jhl.mds.dto.MysqlServerDTO;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class MySQLService {

    public List<String> getDatabases(MysqlServerDTO dto) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:mysql://" + dto.getHost() + ":" + dto.getPort(), dto.getUsername(), dto.getPassword());
        Statement st = conn.createStatement();

        ResultSet rs = st.executeQuery("SHOW DATABASES;");

        List<String> databaseNames = new ArrayList<>();
        while (rs.next()) {
            String tableName = rs.getString(1);
            databaseNames.add(tableName);
        }
        return databaseNames;
    }

    public List<String> getTables(MysqlServerDTO dto, String database) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:mysql://" + dto.getHost() + ":" + dto.getPort() + "/" + database, dto.getUsername(), dto.getPassword());
        Statement st = conn.createStatement();

        ResultSet rs = st.executeQuery("SHOW TABLES;");

        List<String> tableNames = new ArrayList<>();
        while (rs.next()) {
            String tableName = rs.getString(1);
            tableNames.add(tableName);
        }
        return tableNames;
    }

    public List<MysqlFieldDTO> getFields(MysqlServerDTO dto, String database, String table) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:mysql://" + dto.getHost() + ":" + dto.getPort() + "/" + database, dto.getUsername(), dto.getPassword());
        Statement st = conn.createStatement();

        ResultSet rs = st.executeQuery("DESCRIBE " + table + ";");

        List<MysqlFieldDTO> fields = new ArrayList<>();
        while (rs.next()) {
            fields.add(MysqlFieldDTO.builder()
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
}
