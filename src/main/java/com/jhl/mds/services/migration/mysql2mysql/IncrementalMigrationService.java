package com.jhl.mds.services.migration.mysql2mysql;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.TableMapEventData;
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import com.jhl.mds.dto.FullMigrationDTO;
import com.jhl.mds.dto.MySQLServerDTO;
import com.jhl.mds.dto.TaskDTO;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class IncrementalMigrationService {

    private static ExecutorService executor = Executors.newFixedThreadPool(4);
    private Map<MySQLServerDTO, Map<Long, TaskDTO.Table>> tableMap = new HashMap<>();

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

    private void write(FullMigrationDTO dto, WriteRowsEventData data) {
        TaskDTO.Table tableInfo = tableMap.get(dto.getSource()).get(data.getTableId());
        System.out.println("tableInfo = " + tableInfo);
        System.out.println("data = " + data);
    }

    private void putTableMap(MySQLServerDTO dto, TableMapEventData data) {
        if (!tableMap.containsKey(dto)) tableMap.put(dto, new HashMap<>());
        tableMap.get(dto).put(data.getTableId(), new TaskDTO.Table(dto.getServerId(), data.getDatabase(), data.getTable()));
    }
}
