package com.jhl.mds.services.mysql;

import com.jhl.mds.dto.MySQLServerDTO;
import com.jhl.mds.util.ColumnUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class MySQLWriteService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private MySQLConnectionPool mySQLConnectionPool;

    @Autowired
    public MySQLWriteService(MySQLConnectionPool mySQLConnectionPool) {
        this.mySQLConnectionPool = mySQLConnectionPool;
    }

    public Future<?> queue(MySQLServerDTO serverDTO, String database, String table, List<String> columns, String insertDatas) {
        return executor.submit(() -> {
            try {
                run(serverDTO, database, table, columns, insertDatas);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void run(MySQLServerDTO serverDTO, String database, String table, List<String> columns, String insertDatas) throws SQLException {
        Connection conn = mySQLConnectionPool.getConnection(serverDTO);
        Statement st = conn.createStatement();

        String sql = String.format("INSERT INTO %s(%s) VALUES %s;", database + "." + table, ColumnUtil.columnListToString(columns), insertDatas);
        logger.info("Run query: " + sql);

        st.execute(sql);
        st.close();
    }
}
