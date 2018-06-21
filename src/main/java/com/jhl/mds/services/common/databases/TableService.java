package com.jhl.mds.services.common.databases;

import com.jhl.mds.dto.DatabaseDTO;

import java.sql.*;

public class TableService {

    public void getAllTablesOfDatabase(DatabaseDTO dto) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:mysql://" + dto.getHost() + ":" + dto.getPort() + "/" + dto.getDatabase(), dto.getUsername(), dto.getPassword());
        Statement st = conn.createStatement();

        ResultSet rs = st.executeQuery("SHOW TABLES;");
        while (rs.next()) {
            System.out.println("rs = " + rs);
        }
    }
}
