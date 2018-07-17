package com.jhl.mds.services.mysql.binlog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.TableMapEventData;
import com.github.shyiko.mysql.binlog.event.UpdateRowsEventData;
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import com.jhl.mds.dto.MySQLServerDTO;
import com.jhl.mds.dto.TableInfoDTO;
import com.jhl.mds.util.Md5;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MySQLBinLogConnection {

    private static final ObjectMapper jacksonObjectMapper = new ObjectMapper();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String BINLOG_POSITION_FILENAME;
    private final BinaryLogClient binlogClient;
    private Map<Long, TableInfoDTO> tableMap = new HashMap<>();
    private Map<TableInfoDTO, List<MySQLBinLogListener>> listenerMap = new HashMap<>();

    public MySQLBinLogConnection(MySQLServerDTO server) {
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
                            listener.insert(event.getData());
                        }
                    }
                    break;
                case EXT_UPDATE_ROWS:
                    UpdateRowsEventData updateRowsEventData = event.getData();
                    tableInfo = tableMap.get(updateRowsEventData.getTableId());
                    if (listenerMap.containsKey(tableInfo)) {
                        List<MySQLBinLogListener> listeners = listenerMap.get(tableInfo);
                        for (MySQLBinLogListener listener : listeners) {
                            listener.update(event.getData());
                        }
                    }
                    break;
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
        return "binlog_" + server.getHost().replaceAll("\\.", "_") + "_" + server.getPort() + "_" + Md5.generate(server.getUsername() + "_" + server.getPassword()) + ".txt";
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
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(BINLOG_POSITION_FILENAME))) {
            bw.write(jacksonObjectMapper.writeValueAsString(new BinlogPosition(binlogFilename, position)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private BinlogPosition readBinlogPosition() {
        BinlogPosition binlogPosition = null;
        try (BufferedReader br = new BufferedReader(new FileReader(BINLOG_POSITION_FILENAME))) {
            String value = br.readLine().trim();
            binlogPosition = jacksonObjectMapper.readValue(value, BinlogPosition.class);
        } catch (Exception e) {
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
