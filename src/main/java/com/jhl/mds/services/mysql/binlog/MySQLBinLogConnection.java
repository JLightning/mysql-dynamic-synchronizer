package com.jhl.mds.services.mysql.binlog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.DeleteRowsEventData;
import com.github.shyiko.mysql.binlog.event.TableMapEventData;
import com.github.shyiko.mysql.binlog.event.UpdateRowsEventData;
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import com.jhl.mds.dto.MySQLServerDTO;
import com.jhl.mds.dto.TableInfoDTO;
import com.jhl.mds.util.Md5;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MySQLBinLogConnection {

    private static final ObjectMapper jacksonObjectMapper = new ObjectMapper();
    private boolean ignoreBinlogPositionFile;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String BINLOG_POSITION_FILENAME;
    private final BinaryLogClient binlogClient;
    private Map<Long, TableInfoDTO> tableMap = new HashMap<>();
    private Map<TableInfoDTO, List<MySQLBinLogListener>> listenerMap = new HashMap<>();

    public MySQLBinLogConnection(MySQLServerDTO server, boolean ignoreBinlogPositionFile) {
        this.ignoreBinlogPositionFile = ignoreBinlogPositionFile;

        BINLOG_POSITION_FILENAME = getBinlogPositionFilename(server);

        binlogClient = new BinaryLogClient(server.getHost(), Integer.valueOf(server.getPort()), server.getUsername(), server.getPassword());
        binlogClient.registerEventListener(event -> {
            writeBinlogPosition(binlogClient.getBinlogFilename(), binlogClient.getBinlogPosition());
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
                            listener.onInsert(writeRowsEventData);
                        }
                    }
                    break;
                case EXT_UPDATE_ROWS:
                    UpdateRowsEventData updateRowsEventData = event.getData();
                    tableInfo = tableMap.get(updateRowsEventData.getTableId());
                    if (listenerMap.containsKey(tableInfo)) {
                        List<MySQLBinLogListener> listeners = listenerMap.get(tableInfo);
                        for (MySQLBinLogListener listener : listeners) {
                            listener.onUpdate(updateRowsEventData);
                        }
                    }
                    break;
                case EXT_DELETE_ROWS:
                    DeleteRowsEventData deleteRowsEventData = event.getData();
                    tableInfo = tableMap.get(deleteRowsEventData.getTableId());
                    if (listenerMap.containsKey(tableInfo)) {
                        List<MySQLBinLogListener> listeners = listenerMap.get(tableInfo);
                        for (MySQLBinLogListener listener : listeners) {
                            listener.onDelete(deleteRowsEventData);
                        }
                    }
            }
        });

        BinlogPosition binlogPosition = readBinlogPosition();
        if (binlogPosition != null) {
            binlogClient.setBinlogFilename(binlogPosition.getFilename());
            binlogClient.setBinlogPosition(binlogPosition.getPosition());
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

    private String getBinlogPositionFilename(MySQLServerDTO server) {
        return "./store/binlog/binlog_" + server.getHost().replaceAll("\\.", "_") + "_" + server.getPort() + "_" + Md5.generate(server.getUsername() + "_" + server.getPassword()) + ".txt";
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

    private void writeBinlogPosition(String binlogFilename, long position) {
        if (ignoreBinlogPositionFile) return;
        try {
            FileUtils.writeStringToFile(new File(BINLOG_POSITION_FILENAME), jacksonObjectMapper.writeValueAsString(new BinlogPosition(binlogFilename, position)), "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BinlogPosition readBinlogPosition() {
        BinlogPosition binlogPosition = null;
        if (ignoreBinlogPositionFile) return null;
        try {
            String value = FileUtils.readFileToString(new File(BINLOG_POSITION_FILENAME), "utf-8");
            binlogPosition = jacksonObjectMapper.readValue(value, BinlogPosition.class);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return binlogPosition;
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class BinlogPosition {
        private String filename;
        private long position;
    }
}
