package com.jhl.mds.controllers.api;

import com.jhl.mds.consts.MySQLInsertMode;
import com.jhl.mds.dao.entities.MySQLServer;
import com.jhl.mds.dao.entities.RedisServer;
import com.jhl.mds.dao.repositories.MySQLServerRepository;
import com.jhl.mds.dto.*;
import com.jhl.mds.jsclientgenerator.JsClientController;
import com.jhl.mds.services.mysql.MySQLDescribeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mysql")
@JsClientController(className = "MySQLApiClient", fileName = "mysql-api-client")
public class MySQLApiController {

    private final MySQLServerRepository mySQLServerRepository;
    private final MySQLDescribeService mySQLDescribeService;
    private final MySQLServerDTO.Converter mysqlServerDtoConverter;

    @Autowired
    public MySQLApiController(
            MySQLServerRepository mySQLServerRepository,
            MySQLDescribeService mySQLDescribeService,
            MySQLServerDTO.Converter mysqlServerDtoConverter
    ) {
        this.mySQLServerRepository = mySQLServerRepository;
        this.mySQLDescribeService = mySQLDescribeService;
        this.mysqlServerDtoConverter = mysqlServerDtoConverter;
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse exceptionHandler(Exception e) {
        return ApiResponse.error(e);
    }

    @GetMapping("/")
    public ApiResponse<List<MySQLServerDTO>> list() {
        return ApiResponse.success(mySQLServerRepository.findAll().stream().map(mysqlServerDtoConverter::from).collect(Collectors.toList()));
    }

    @PutMapping("/")
    public ApiResponse<MySQLServerDTO> create(@RequestBody MySQLServerDTO dto) {
        MySQLServer mySQLServer = mysqlServerDtoConverter.toDAO(dto);
        mySQLServerRepository.save(mySQLServer);
        dto.setServerId(mySQLServer.getServerId());
        return ApiResponse.success(dto);
    }

    @PostMapping("/")
    public ApiResponse<MySQLServerDTO> update(@RequestBody MySQLServerDTO dto) {
        MySQLServer mySQLServer = mysqlServerDtoConverter.toDAO(dto);
        mySQLServerRepository.save(mySQLServer);
        dto.setServerId(mySQLServer.getServerId());
        return ApiResponse.success(dto);
    }

    @GetMapping("/{serverId}")
    public ApiResponse<MySQLServerDTO> detail(@PathVariable int serverId) {
        MySQLServer mysqlServer = mySQLServerRepository.findByServerId(serverId);
        MySQLServerDTO dto = mysqlServerDtoConverter.from(mysqlServer);
        return ApiResponse.success(dto);
    }

    @DeleteMapping("/{serverId}")
    public ApiResponse<Boolean> delete(@PathVariable int serverId) {
        mySQLServerRepository.deleteById(serverId);
        return ApiResponse.success(true);
    }

    @GetMapping("/databases")
    public ApiResponse<List<String>> getDatabasesForServer(@RequestParam int serverId) throws SQLException {
        MySQLServer server = mySQLServerRepository.getOne(serverId);
        return ApiResponse.success(mySQLDescribeService.getDatabases(mysqlServerDtoConverter.from(server)));
    }

    @GetMapping("/tables")
    public ApiResponse<List<String>> getTablesForServerAndDatabase(@RequestParam int serverId, @RequestParam String database) throws SQLException {
        MySQLServer server = mySQLServerRepository.getOne(serverId);
        return ApiResponse.success(mySQLDescribeService.getTables(mysqlServerDtoConverter.from(server), database));
    }

    @GetMapping("/fields")
    public ApiResponse<List<MySQLFieldDTO>> getFieldForServerDatabaseAndTable(@RequestParam int serverId, @RequestParam String database, @RequestParam String table) throws SQLException {
        MySQLServer server = mySQLServerRepository.getOne(serverId);
        return ApiResponse.success(mySQLDescribeService.getFields(mysqlServerDtoConverter.from(server), database, table));
    }

    @PostMapping("/fields-mapping")
    public ApiResponse<List<MySQLFieldWithMappingDTO>> getMappingFor2Table(@RequestBody TableFieldsMappingRequestDTO dto) throws SQLException {
        MySQLServer sourceServer = mySQLServerRepository.getOne(dto.getSourceServerId());
        MySQLServer targetServer = mySQLServerRepository.getOne(dto.getTargetServerId());
        return ApiResponse.success(mySQLDescribeService.getFieldsMappingFor2Table(mysqlServerDtoConverter.from(sourceServer), mysqlServerDtoConverter.from(targetServer), dto));
    }

    @PostMapping("/validate-filter")
    public ApiResponse<String> validateFilter(@RequestParam int serverId, @RequestParam String database, @RequestParam String table, @RequestParam String filter) throws Exception {
        MySQLServer server = mySQLServerRepository.getOne(serverId);
        mySQLDescribeService.validateFilter(mysqlServerDtoConverter.from(server), database, table, filter);
        return ApiResponse.success(mySQLDescribeService.beautifyFilter(filter));
    }

    @GetMapping("/get-insert-modes")
    public ApiResponse<MySQLInsertMode[]> getInsertModes() {
        return ApiResponse.success(MySQLInsertMode.values());
    }

}
