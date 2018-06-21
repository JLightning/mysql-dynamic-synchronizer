package com.jhl.mds.services.common.databases;

import com.jhl.mds.dto.DatabaseDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TableService extends AbstractDatabaseService {

    public List<String> getAllTablesOfDatabase(DatabaseDTO dto) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:mysql://" + dto.getHost() + ":" + dto.getPort() + "/" + dto.getDatabase(), dto.getUsername(), dto.getPassword());
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
