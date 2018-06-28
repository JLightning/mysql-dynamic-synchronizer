package com.jhl.mds.controllers.api;

import com.jhl.mds.dao.entities.MySQLServer;
import com.jhl.mds.dao.repositories.MysqlServerRepository;
import com.jhl.mds.dto.*;
import com.jhl.mds.services.mysql.MySQLDescribeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mysql")
public class MySQLApiController {

    private final MysqlServerRepository mysqlServerRepository;
    private final MySQLDescribeService mySQLDescribeService;
    private final MySQLServerDTO.Converter mysqlServerDtoConverter;

    @Autowired
    public MySQLApiController(
            MysqlServerRepository mysqlServerRepository,
            MySQLDescribeService mySQLDescribeService,
            MySQLServerDTO.Converter mysqlServerDtoConverter
    ) {
        this.mysqlServerRepository = mysqlServerRepository;
        this.mySQLDescribeService = mySQLDescribeService;
        this.mysqlServerDtoConverter = mysqlServerDtoConverter;
    }

    @GetMapping("/servers")
    public ApiResponse<List<MySQLServerDTO>> getServers() {
        return ApiResponse.success(mysqlServerRepository.findAll().stream().map(mysqlServerDtoConverter::from).collect(Collectors.toList()));
    }

    @GetMapping("/databases")
    public ApiResponse<List<String>> getDatabasesForServer(@RequestParam int serverId) {
        Optional<MySQLServer> opt = mysqlServerRepository.findById(serverId);
        if (!opt.isPresent()) {
            return ApiResponse.error("server not found");
        }
        MySQLServer server = opt.get();

        try {
            return ApiResponse.success(mySQLDescribeService.getDatabases(mysqlServerDtoConverter.from(server)));
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

        try {
            return ApiResponse.success(mySQLDescribeService.getTables(mysqlServerDtoConverter.from(server), database));
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
            return ApiResponse.success(mySQLDescribeService.getFields(mysqlServerDtoConverter.from(server), database, table));
        } catch (SQLException e) {
            return ApiResponse.error(e);
        }
    }

    @PostMapping("/fields-mapping")
    public ApiResponse<List<MySQLFieldWithMappingDTO>> getMappingFor2Table(@RequestBody TableFieldsMappingDTO dto) {
        Optional<MySQLServer> opt = mysqlServerRepository.findById(dto.getSourceServerId());
        if (!opt.isPresent()) {
            return ApiResponse.error("source server not found");
        }
        MySQLServer sourceServer = opt.get();

        opt = mysqlServerRepository.findById(dto.getTargetServerId());
        if (!opt.isPresent()) {
            return ApiResponse.error("target server not found");
        }
        MySQLServer targetServer = opt.get();

        try {
            return ApiResponse.success(mySQLDescribeService.getFieldsMappingFor2Table(mysqlServerDtoConverter.from(sourceServer), mysqlServerDtoConverter.from(targetServer), dto));
        } catch (SQLException e) {
            return ApiResponse.error(e);
        }
    }
}
