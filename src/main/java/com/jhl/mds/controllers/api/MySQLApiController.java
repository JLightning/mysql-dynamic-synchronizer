package com.jhl.mds.controllers.api;

import com.jhl.mds.dao.entities.MysqlServer;
import com.jhl.mds.dao.repositories.MysqlServerRepository;
import com.jhl.mds.dto.MysqlFieldDTO;
import com.jhl.mds.dto.MysqlServerDTO;
import com.jhl.mds.services.database.MySQLService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/mysql")
public class MySQLApiController {

    private final MysqlServerRepository mysqlServerRepository;
    private final MySQLService mySQLService;

    @Autowired
    public MySQLApiController(
            MysqlServerRepository mysqlServerRepository,
            MySQLService mySQLService
    ) {
        this.mysqlServerRepository = mysqlServerRepository;
        this.mySQLService = mySQLService;
    }

    @GetMapping("/databases")
    public List<String> getDatabasesForServer(@RequestParam int serverId) throws SQLException {
        Optional<MysqlServer> opt = mysqlServerRepository.findById(serverId);
        MysqlServer server = opt.get();

        return mySQLService.getDatabases(genDTO(server));
    }

    @GetMapping("/tables")
    public List<String> getTablesForServerAndDatabase(@RequestParam int serverId, @RequestParam String database) throws SQLException {
        Optional<MysqlServer> opt = mysqlServerRepository.findById(serverId);
        MysqlServer server = opt.get();

        return mySQLService.getTables(genDTO(server), database);
    }

    @GetMapping("/fields")
    public List<MysqlFieldDTO> getFieldForServerDatabaseAndTable(@RequestParam int serverId, @RequestParam String database, @RequestParam String table) throws SQLException {
        Optional<MysqlServer> opt = mysqlServerRepository.findById(serverId);
        MysqlServer server = opt.get();

        return mySQLService.getFields(genDTO(server), database, table);
    }

    private MysqlServerDTO genDTO(MysqlServer server) {
        return MysqlServerDTO.builder()
                .host(server.getHost())
                .port(server.getPort())
                .username(server.getUsername())
                .password(server.getPassword())
                .build();
    }
}
