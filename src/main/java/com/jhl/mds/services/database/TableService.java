package com.jhl.mds.services.database;

import com.jhl.mds.dto.MysqlServerDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TableService extends AbstractDatabaseService {

    public List<String> getAllTablesOfDatabase(MysqlServerDTO dto) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:mysql://" + dto.getHost() + ":" + dto.getPort(), dto.getUsername(), dto.getPassword());
        Statement st = conn.createStatement();

        ResultSet rs = st.executeQuery("SHOW TABLES;");

        List<String> tableNames = new ArrayList<>();
        while (rs.next()) {
            String tableName = rs.getString(1);
            tableNames.add(tableName);
        }
        return tableNames;
    }
}
