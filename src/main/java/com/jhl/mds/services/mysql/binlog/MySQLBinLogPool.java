package com.jhl.mds.services.mysql.binlog;

import com.jhl.mds.dao.repositories.MySQLBinLogPositionRepository;
import com.jhl.mds.dto.MySQLServerDTO;
import com.jhl.mds.dto.TableInfoDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MySQLBinLogPool {

    @Value("${mds.incremental.ignoreBinlogPositionFile:false}")
    private boolean ignoreBinlogPositionFile;
    private Map<MySQLServerDTO, MySQLBinLogConnection> connectionMap = new HashMap<>();
    private MySQLBinLogPositionRepository mySQLBinLogPositionRepository;

    public MySQLBinLogPool(
            MySQLBinLogPositionRepository mySQLBinLogPositionRepository
    ) {
        this.mySQLBinLogPositionRepository = mySQLBinLogPositionRepository;
    }

    public void openNewConnection(MySQLServerDTO serverDTO) {
        if (!connectionMap.containsKey(serverDTO)) {
            connectionMap.put(serverDTO, new MySQLBinLogConnection(mySQLBinLogPositionRepository, serverDTO, ignoreBinlogPositionFile));
        }
    }

    public synchronized void addListener(TableInfoDTO source, MySQLBinLogListener listener) {
        if (!connectionMap.containsKey(source.getServer())) {
            openNewConnection(source.getServer());
        }
        MySQLBinLogConnection connection = connectionMap.get(source.getServer());
        if (connection != null) {
            connection.addListener(source, listener);
        }
    }

    public synchronized void removeListener(TableInfoDTO source, MySQLBinLogListener listener) {
        if (!connectionMap.containsKey(source.getServer())) {
            return;
        }
        MySQLBinLogConnection connection = connectionMap.get(source.getServer());
        if (connection != null) {
            connection.removeListener(source, listener);
        }
    }
}
