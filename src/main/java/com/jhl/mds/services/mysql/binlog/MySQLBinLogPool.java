package com.jhl.mds.services.mysql.binlog;

import com.jhl.mds.dao.entities.MySQLServer;
import com.jhl.mds.dao.repositories.MySQLServerRepository;
import com.jhl.mds.dto.MySQLServerDTO;
import com.jhl.mds.dto.TableInfoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
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

    @PostConstruct
    private void init() {
        List<MySQLServer> servers = mySQLServerRepository.findAll();
        for (MySQLServer server : servers) {
            MySQLServerDTO serverDTO = mySQLServerDTOConverter.from(server);
            connectionMap.put(serverDTO, new MySQLBinLogConnection(serverDTO));
        }
    }

    public void addListener(TableInfoDTO source, MySQLBinLogListener listener) {
        MySQLBinLogConnection connection = connectionMap.get(source.getServer());
        if (connection != null) {
            connection.addListener(source, listener);
        }
    }
}
