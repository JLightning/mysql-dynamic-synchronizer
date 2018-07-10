package com.jhl.mds.services.mysql.binlog;

import com.jhl.mds.dao.repositories.MySQLServerRepository;
import com.jhl.mds.dto.MySQLServerDTO;
import com.jhl.mds.dto.TableInfoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MySQLBinLogPool {

    private MySQLServerRepository mySQLServerRepository;
    private MySQLServerDTO.Converter mySQLServerDTOConverter;
    private Map<MySQLServerDTO, MySQLBinLogConnection> connectionMap = new HashMap<>();

    @Autowired
    public MySQLBinLogPool(
            MySQLServerRepository mySQLServerRepository,
            MySQLServerDTO.Converter mySQLServerDTOConverter
    ) {
        this.mySQLServerRepository = mySQLServerRepository;
        this.mySQLServerDTOConverter = mySQLServerDTOConverter;
    }

    public void openNewConnection(MySQLServerDTO serverDTO) {
        if (!connectionMap.containsKey(serverDTO)) {
            connectionMap.put(serverDTO, new MySQLBinLogConnection(serverDTO));
        }
    }

    public void addListener(TableInfoDTO source, MySQLBinLogListener listener) {
        if (!connectionMap.containsKey(source.getServer())) {
            openNewConnection(source.getServer());
        }
        MySQLBinLogConnection connection = connectionMap.get(source.getServer());
        if (connection != null) {
            connection.addListener(source, listener);
        }
    }
}
