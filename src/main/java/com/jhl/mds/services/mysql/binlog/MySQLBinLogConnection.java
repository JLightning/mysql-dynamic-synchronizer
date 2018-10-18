package com.jhl.mds.services.mysql.binlog;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.DeleteRowsEventData;
import com.github.shyiko.mysql.binlog.event.TableMapEventData;
import com.github.shyiko.mysql.binlog.event.UpdateRowsEventData;
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import com.jhl.mds.dao.entities.MySQLBinLogPosition;
import com.jhl.mds.dao.repositories.MySQLBinLogPositionRepository;
import com.jhl.mds.dto.MySQLServerDTO;
import com.jhl.mds.dto.TableInfoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MySQLBinLogConnection {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final BinaryLogClient binlogClient;
    private Map<Long, TableInfoDTO> tableMap = new HashMap<>();
    private Map<TableInfoDTO, List<MySQLBinLogListener>> listenerMap = new HashMap<>();

    public MySQLBinLogConnection(MySQLBinLogPositionRepository mySQLBinLogPositionRepository, MySQLServerDTO server, boolean ignoreBinlogPositionFile) {
        binlogClient = new BinaryLogClient(server.getHost(), Integer.valueOf(server.getPort()), server.getUsername(), server.getPassword());
        binlogClient.registerEventListener(event -> {
            if (!ignoreBinlogPositionFile)
                mySQLBinLogPositionRepository.updatePosition(server.getHost(), server.getPort(), binlogClient.getBinlogFilename(), binlogClient.getBinlogPosition());

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
                            listener.insert(writeRowsEventData);
                        }
                    }
                    break;
                case EXT_UPDATE_ROWS:
                    UpdateRowsEventData updateRowsEventData = event.getData();
                    tableInfo = tableMap.get(updateRowsEventData.getTableId());
                    if (listenerMap.containsKey(tableInfo)) {
                        List<MySQLBinLogListener> listeners = listenerMap.get(tableInfo);
                        for (MySQLBinLogListener listener : listeners) {
                            listener.update(updateRowsEventData);
                        }
                    }
                    break;
                case EXT_DELETE_ROWS:
                    DeleteRowsEventData deleteRowsEventData = event.getData();
                    tableInfo = tableMap.get(deleteRowsEventData.getTableId());
                    if (listenerMap.containsKey(tableInfo)) {
                        List<MySQLBinLogListener> listeners = listenerMap.get(tableInfo);
                        for (MySQLBinLogListener listener : listeners) {
                            listener.delete(deleteRowsEventData);
                        }
                    }
            }
        });

        if (!ignoreBinlogPositionFile) {
            MySQLBinLogPosition binlogPosition = mySQLBinLogPositionRepository.findByHostAndPort(server.getHost(), server.getPort());
            if (binlogPosition != null) {
                binlogClient.setBinlogFilename(binlogPosition.getFilename());
                binlogClient.setBinlogPosition(binlogPosition.getPosition());
            }
        }

        new Thread(() -> {
            try {
                logger.info("Open new binlog connection to: " + server);
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

    public void removeListener(TableInfoDTO tableInfoDTO, MySQLBinLogListener listener) {
        List<MySQLBinLogListener> list = listenerMap.get(tableInfoDTO);
        if (list == null) return;
        list.remove(listener);
        listenerMap.put(tableInfoDTO, list);
    }
}
