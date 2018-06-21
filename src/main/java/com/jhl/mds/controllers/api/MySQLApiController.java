package com.jhl.mds.controllers.api;

import com.jhl.mds.dao.entities.MysqlServer;
import com.jhl.mds.dao.repositories.MysqlServerRepository;
import com.jhl.mds.dto.MysqlFieldDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/mysql")
public class MySQLApiController {

    private final MysqlServerRepository mysqlServerRepository;

    @Autowired
    public MySQLApiController(MysqlServerRepository mysqlServerRepository) {
        this.mysqlServerRepository = mysqlServerRepository;
    }

    @GetMapping("/database-by-server")
    public List<String> getDatabasesForServer(@RequestParam int serverId) throws SQLException {
        Optional<MysqlServer> opt = mysqlServerRepository.findById(serverId);
        MysqlServer server = opt.get();

        Connection conn = DriverManager.getConnection("jdbc:mysql://" + server.getHost() + ":" + server.getPort(), server.getUsername(), server.getPassword());
        Statement st = conn.createStatement();

        ResultSet rs = st.executeQuery("SHOW DATABASES;");

        List<String> databaseNames = new ArrayList<>();
        while (rs.next()) {
            String tableName = rs.getString(1);
            databaseNames.add(tableName);
        }
        return databaseNames;
    }

    @GetMapping("/table-by-server-and-database")
    public List<String> getTablesForServerAndDatabase(@RequestParam int serverId, @RequestParam String database) throws SQLException {
        Optional<MysqlServer> opt = mysqlServerRepository.findById(serverId);
        MysqlServer server = opt.get();

        Connection conn = DriverManager.getConnection("jdbc:mysql://" + server.getHost() + ":" + server.getPort() + "/" + database, server.getUsername(), server.getPassword());
        Statement st = conn.createStatement();

        ResultSet rs = st.executeQuery("SHOW TABLES;");

        List<String> tableNames = new ArrayList<>();
        while (rs.next()) {
            String tableName = rs.getString(1);
            tableNames.add(tableName);
        }
        return tableNames;
    }

    @GetMapping("/field-by-server-database-and-table")
    public List<MysqlFieldDTO> getFieldForServerDatabaseAndTable(@RequestParam int serverId, @RequestParam String database, @RequestParam String table) throws SQLException {
        Optional<MysqlServer> opt = mysqlServerRepository.findById(serverId);
        MysqlServer server = opt.get();

        Connection conn = DriverManager.getConnection("jdbc:mysql://" + server.getHost() + ":" + server.getPort() + "/" + database, server.getUsername(), server.getPassword());
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
