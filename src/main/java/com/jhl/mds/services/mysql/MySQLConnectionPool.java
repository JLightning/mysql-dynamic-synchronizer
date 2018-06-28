package com.jhl.mds.services.mysql;

import com.jhl.mds.dto.MySQLServerDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

@Service
public class MySQLConnectionPool {

    private static final int MAX_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private final Random rand = new Random();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Map<MySQLServerDTO, List<Connection>> connections = new HashMap<>();

    public Connection getConnection(MySQLServerDTO dto) throws SQLException {
        if (!connections.containsKey(dto)) {
            List<Connection> conns = new ArrayList<>();
            for (int i = 0; i < MAX_POOL_SIZE; i++) {
                conns.add(DriverManager.getConnection("jdbc:mysql://" + dto.getHost() + ":" + dto.getPort(), dto.getUsername(), dto.getPassword()));
            }
            connections.put(dto, conns);
        }
        return connections.get(dto).get(rand.nextInt(MAX_POOL_SIZE));
    }

    @PreDestroy
    public void destroy() {
        for (Map.Entry<MySQLServerDTO, List<Connection>> entry : connections.entrySet()) {
            try {
                logger.info("Close connection for mysql server: {}", entry.getKey());
                for (Connection conn : entry.getValue()) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("Cannot close connection for mysql server: {}, exception: {}", entry.getKey(), e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
