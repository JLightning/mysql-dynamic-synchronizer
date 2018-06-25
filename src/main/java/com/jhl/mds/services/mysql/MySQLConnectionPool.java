package com.jhl.mds.services.mysql;

import com.jhl.mds.dto.MySQLServerDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Service
public class MySQLConnectionPool {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    public Map<MySQLServerDTO, Connection> connections = new HashMap<>();

    public Connection getConnection(MySQLServerDTO dto) throws SQLException {
        if (!connections.containsKey(dto)) {
            connections.put(dto, DriverManager.getConnection("jdbc:mysql://" + dto.getHost() + ":" + dto.getPort(), dto.getUsername(), dto.getPassword()));
        }
        return connections.get(dto);
    }

    @PreDestroy
    public void destroy() {
        for (Map.Entry<MySQLServerDTO, Connection> entry: connections.entrySet()) {
            try {
                logger.info("Close connection for mysql server: {}", entry.getKey());
                entry.getValue().close();
            } catch (SQLException e) {
                logger.error("Cannot close connection for mysql server: {}, exception: {}", entry.getKey(), e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
