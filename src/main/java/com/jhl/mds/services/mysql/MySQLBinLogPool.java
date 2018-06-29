package com.jhl.mds.services.mysql;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.TableMapEventData;
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import com.jhl.mds.dao.entities.MySQLServer;
import com.jhl.mds.dao.repositories.MySQLServerRepository;
import com.jhl.mds.dto.MySQLServerDTO;
import com.jhl.mds.dto.TableInfoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MySQLBinLogPool {

    private MySQLServerRepository mySQLServerRepository;
    private MySQLServerDTO.Converter mySQLServerDTOConverter;
    private Map<MySQLServerDTO, Map<Long, TableInfoDTO>> tableMap = new HashMap<>();
    private Map<TableInfoDTO, List<Listener>> listenerMap = new HashMap<>();

    @Autowired
    public MySQLBinLogPool(
            MySQLServerRepository mySQLServerRepository,
            MySQLServerDTO.Converter mySQLServerDTOConverter
    ) {
        this.mySQLServerRepository = mySQLServerRepository;
        this.mySQLServerDTOConverter = mySQLServerDTOConverter;
    }

    @PostConstruct
    private void init() {
        List<MySQLServer> servers = mySQLServerRepository.findAll();
        for (MySQLServer server : servers) {
            new Thread(() -> openBinLogConnection(mySQLServerDTOConverter.from(server))).start();
        }
    }

    private void openBinLogConnection(MySQLServerDTO server) {
        try {
            BinaryLogClient client = new BinaryLogClient(server.getHost(), Integer.valueOf(server.getPort()), server.getUsername(), server.getPassword());
            client.registerEventListener(event -> {
                System.out.println("event = " + event);
                switch (event.getHeader().getEventType()) {
                    case TABLE_MAP:
                        putTableMap(server, event.getData());
                        break;
                    case EXT_WRITE_ROWS:
                        TableInfoDTO tableInfo = tableMap.get(server).get(((WriteRowsEventData) event.getData()).getTableId());
                        if (listenerMap.containsKey(tableInfo)) {
                            List<Listener> listeners = listenerMap.get(tableInfo);
                            for (Listener listener : listeners) {
                                listener.write(event.getData());
                            }
                        }
                        break;
                }
            });
            client.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void putTableMap(MySQLServerDTO server, TableMapEventData data) {
        if (!tableMap.containsKey(server)) tableMap.put(server, new HashMap<>());
        tableMap.get(server).put(data.getTableId(), new TableInfoDTO(server, data.getDatabase(), data.getTable()));
    }

    public void addListener(TableInfoDTO source, Listener listener) {
        if (!listenerMap.containsKey(source)) listenerMap.put(source, new ArrayList<>());
        List<Listener> listListener = listenerMap.get(source);
        listListener.add(listener);
        listenerMap.put(source, listListener);
    }

    public interface Listener {

        void write(WriteRowsEventData eventData);
    }
}
