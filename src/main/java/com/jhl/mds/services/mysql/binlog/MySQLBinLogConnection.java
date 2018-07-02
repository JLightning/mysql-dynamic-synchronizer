package com.jhl.mds.services.mysql.binlog;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.TableMapEventData;
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import com.jhl.mds.dto.MySQLServerDTO;
import com.jhl.mds.dto.TableInfoDTO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MySQLBinLogConnection {

    private final BinaryLogClient binlogClient;
    private Map<Long, TableInfoDTO> tableMap = new HashMap<>();
    private Map<TableInfoDTO, List<MySQLBinLogListener>> listenerMap = new HashMap<>();

    public MySQLBinLogConnection(MySQLServerDTO server) {
        binlogClient = new BinaryLogClient(server.getHost(), Integer.valueOf(server.getPort()), server.getUsername(), server.getPassword());
        binlogClient.registerEventListener(event -> {
            System.out.println("event = " + event);
            switch (event.getHeader().getEventType()) {
                case TABLE_MAP:
                    putTableMap(server, event.getData());
                    break;
                case EXT_WRITE_ROWS:
                    WriteRowsEventData writeRowsEventData = event.getData();
                    TableInfoDTO tableInfo = tableMap.get(writeRowsEventData.getTableId());
                    if (listenerMap.containsKey(tableInfo)) {
                        List<MySQLBinLogListener> listeners = listenerMap.get(tableInfo);
                        for (MySQLBinLogListener listener : listeners) {
                            listener.insert(event.getData());
                        }
                    }
                    break;
            }
        });

        new Thread(() -> {
            try {
                binlogClient.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void putTableMap(MySQLServerDTO server, TableMapEventData data) {
        tableMap.put(data.getTableId(), new TableInfoDTO(server, data.getDatabase(), data.getTable()));
    }

    public void addListener(TableInfoDTO tableInfoDTO, MySQLBinLogListener listener) {
        List<MySQLBinLogListener> list = listenerMap.get(tableInfoDTO);
        if (list == null) list = new ArrayList<>();
        list.add(listener);
        listenerMap.put(tableInfoDTO, list);
    }
}
