package com.jhl.mds.services.migration.mysql2mysql;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.TableMapEventData;
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import com.jhl.mds.dto.FullMigrationDTO;
import com.jhl.mds.dto.MySQLServerDTO;
import com.jhl.mds.dto.TaskDTO;
import com.jhl.mds.services.mysql.MySQLBinLogService;
import com.jhl.mds.services.mysql.MySQLWriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class IncrementalMigrationService {

    private static ExecutorService executor = Executors.newFixedThreadPool(4);
    private Map<MySQLServerDTO, Map<Long, TaskDTO.Table>> tableMap = new HashMap<>();
    private MySQLBinLogService mySQLBinLogService;
    private MigrationMapperService.Factory migrationMapperServiceFactory;
    private MySQLWriteService mySQLWriteService;

    @Autowired
    public IncrementalMigrationService(MySQLBinLogService mySQLBinLogService, MigrationMapperService.Factory migrationMapperServiceFactory, MySQLWriteService mySQLWriteService) {
        this.mySQLBinLogService = mySQLBinLogService;
        this.migrationMapperServiceFactory = migrationMapperServiceFactory;
        this.mySQLWriteService = mySQLWriteService;
    }

    public void async(FullMigrationDTO dto) {
        executor.submit(() -> {
            try {
                run(dto);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void run(FullMigrationDTO dto) throws Exception {
        MySQLServerDTO source = dto.getSource();
        BinaryLogClient client = new BinaryLogClient(source.getHost(), Integer.valueOf(source.getPort()), source.getUsername(), source.getPassword());
        client.registerEventListener(event -> {
            System.out.println("event = " + event);
            switch (event.getHeader().getEventType()) {
                case TABLE_MAP:
                    putTableMap(dto.getSource(), event.getData());
                    break;
                case EXT_WRITE_ROWS:
                    write(dto, event.getData());
            }
        });
        client.connect();
    }

    private void write(FullMigrationDTO dto, WriteRowsEventData eventData) {
        TaskDTO.Table tableInfo = tableMap.get(dto.getSource()).get(eventData.getTableId());
        TaskDTO taskDTO = dto.getTaskDTO();
        if (!tableInfo.getDatabase().equals(taskDTO.getSource().getDatabase()) || !tableInfo.getTable().equals(taskDTO.getSource().getTable())) {
            return;
        }

        try {
            List<Map<String, Object>> data = mySQLBinLogService.mapDataToField(dto.getSource(), tableInfo, eventData);
            MigrationMapperService migrationMapperService = migrationMapperServiceFactory.create(dto.getTarget(), taskDTO.getTarget(), taskDTO.getMapping());
            String insertDataList = data.stream().map(migrationMapperService::mapToString).collect(Collectors.joining(", "));

            mySQLWriteService.queue(dto.getTarget(), taskDTO.getTarget().getDatabase(), taskDTO.getTarget().getTable(), migrationMapperService.getColumns(), insertDataList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void putTableMap(MySQLServerDTO dto, TableMapEventData data) {
        if (!tableMap.containsKey(dto)) tableMap.put(dto, new HashMap<>());
        tableMap.get(dto).put(data.getTableId(), new TaskDTO.Table(dto.getServerId(), data.getDatabase(), data.getTable()));
    }
}
