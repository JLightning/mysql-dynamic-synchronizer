package com.jhl.mds.services.migration.mysql2mysql;

import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import com.jhl.mds.dto.FullMigrationDTO;
import com.jhl.mds.services.mysql.MySQLWriteService;
import com.jhl.mds.services.mysql.binlog.MySQLBinLogListener;
import com.jhl.mds.services.mysql.binlog.MySQLBinLogPool;
import com.jhl.mds.services.mysql.binlog.MySQLBinLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class IncrementalMigrationService {

    private MySQLBinLogPool mySQLBinLogPool;
    private MySQLBinLogService mySQLBinLogService;
    private MigrationMapperService.Factory migrationMapperServiceFactory;
    private MySQLWriteService mySQLWriteService;

    @Autowired
    public IncrementalMigrationService(MySQLBinLogPool mySQLBinLogPool, MySQLBinLogService mySQLBinLogService, MigrationMapperService.Factory migrationMapperServiceFactory, MySQLWriteService mySQLWriteService) {
        this.mySQLBinLogPool = mySQLBinLogPool;
        this.mySQLBinLogService = mySQLBinLogService;
        this.migrationMapperServiceFactory = migrationMapperServiceFactory;
        this.mySQLWriteService = mySQLWriteService;
    }

    public void run(FullMigrationDTO dto) {
        mySQLBinLogPool.addListener(dto.getSource(), new MySQLBinLogListener() {
            @Override
            public void insert(WriteRowsEventData eventData) {
                IncrementalMigrationService.this.write(dto, eventData);
            }
        });
    }

    private void write(FullMigrationDTO dto, WriteRowsEventData eventData) {
        try {
            List<Map<String, Object>> data = mySQLBinLogService.mapDataToField(dto.getSource(), eventData);
            MigrationMapperService migrationMapperService = migrationMapperServiceFactory.create(dto.getTarget(), dto.getMapping());
            String insertDataList = data.stream().map(migrationMapperService::mapToString).collect(Collectors.joining(", "));

            mySQLWriteService.queue(dto.getTarget(), migrationMapperService.getColumns(), insertDataList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
