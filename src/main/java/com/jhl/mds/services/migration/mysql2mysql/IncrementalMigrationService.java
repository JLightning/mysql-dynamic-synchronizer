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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class IncrementalMigrationService {

    private static ExecutorService executor = Executors.newFixedThreadPool(4);
    private MySQLBinLogPool mySQLBinLogPool;
    private MySQLBinLogService mySQLBinLogService;
    private MigrationMapperService.Factory migrationMapperServiceFactory;
    private MySQLWriteService mySQLWriteService;

    @Autowired
    public IncrementalMigrationService(
            MySQLBinLogPool mySQLBinLogPool,
            MySQLBinLogService mySQLBinLogService,
            MigrationMapperService.Factory migrationMapperServiceFactory,
            MySQLWriteService mySQLWriteService
    ) {
        this.mySQLBinLogPool = mySQLBinLogPool;
        this.mySQLBinLogService = mySQLBinLogService;
        this.migrationMapperServiceFactory = migrationMapperServiceFactory;
        this.mySQLWriteService = mySQLWriteService;
    }

    public void run(FullMigrationDTO dto) {
        mySQLBinLogPool.addListener(dto.getSource(), new MySQLBinLogListener() {
            @Override
            public void insert(WriteRowsEventData eventData) {
                executor.submit(() -> IncrementalMigrationService.this.insert(dto, eventData));
            }
        });
    }

    private void insert(FullMigrationDTO dto, WriteRowsEventData eventData) {
        try {
            List<Map<String, Object>> data = mySQLBinLogService.mapDataToField(dto.getSource(), eventData);
            MigrationMapperService migrationMapperService = migrationMapperServiceFactory.create(dto.getTarget(), dto.getMapping());
            data.forEach(item -> migrationMapperService.queueMapToString(item, insertData -> mySQLWriteService.queue(
                    dto.getTarget(), new MySQLWriteService.WriteInfo(migrationMapperService.getColumns(), insertData, null)
            )));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
