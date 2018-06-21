package com.jhl.mds.controllers.api;

import com.jhl.mds.dao.entities.MySQLServer;
import com.jhl.mds.dao.repositories.MysqlServerRepository;
import com.jhl.mds.dto.ApiResponse;
import com.jhl.mds.dto.MySQLFieldDTO;
import com.jhl.mds.dto.MySQLServerDTO;
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
    private final MySQLServerDTO.Converter mysqlServerDtoConverter;

    @Autowired
    public MySQLApiController(
            MysqlServerRepository mysqlServerRepository,
            MySQLService mySQLService,
            MySQLServerDTO.Converter mysqlServerDtoConverter
    ) {
        this.mysqlServerRepository = mysqlServerRepository;
        this.mySQLService = mySQLService;
        this.mysqlServerDtoConverter = mysqlServerDtoConverter;
    }

    @GetMapping("/databases")
    public ApiResponse<List<String>> getDatabasesForServer(@RequestParam int serverId) {
        Optional<MySQLServer> opt = mysqlServerRepository.findById(serverId);
        if (!opt.isPresent()) {
            return ApiResponse.error("server not found");
        }
        MySQLServer server = opt.get();

        try {
            return ApiResponse.success(mySQLService.getDatabases(mysqlServerDtoConverter.from(server)));
        } catch (SQLException e) {
            return ApiResponse.error(e);
        }
    }

    @GetMapping("/tables")
    public ApiResponse<List<String>> getTablesForServerAndDatabase(@RequestParam int serverId, @RequestParam String database) {
        Optional<MySQLServer> opt = mysqlServerRepository.findById(serverId);
        if (!opt.isPresent()) {
            return ApiResponse.error("server not found");
        }
        MySQLServer server = opt.get();

        List<String> result = null;
        try {
            return ApiResponse.success(mySQLService.getTables(mysqlServerDtoConverter.from(server), database));
        } catch (SQLException e) {
            return ApiResponse.error(e);
        }
    }

    @GetMapping("/fields")
    public ApiResponse<List<MySQLFieldDTO>> getFieldForServerDatabaseAndTable(@RequestParam int serverId, @RequestParam String database, @RequestParam String table) {
        Optional<MySQLServer> opt = mysqlServerRepository.findById(serverId);
        if (!opt.isPresent()) {
            return ApiResponse.error("server not found");
        }
        MySQLServer server = opt.get();
        try {
            return ApiResponse.success(mySQLService.getFields(mysqlServerDtoConverter.from(server), database, table));
        } catch (SQLException e) {
            return ApiResponse.error(e);
        }
    }
}
