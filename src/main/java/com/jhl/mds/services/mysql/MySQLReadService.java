package com.jhl.mds.services.mysql;

import com.jhl.mds.dto.MySQLServerDTO;
import com.jhl.mds.dto.TaskDTO;
import com.jhl.mds.util.MySQLStringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class MySQLReadService {

    private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private MySQLConnectionPool mySQLConnectionPool;

    @Autowired
    public MySQLReadService(MySQLConnectionPool mySQLConnectionPool) {
        this.mySQLConnectionPool = mySQLConnectionPool;
    }

    public Future<?> async(MySQLServerDTO serverDTO, TaskDTO.Table tableInfo, List<String> columns, ResultCallback resultCallback) {
        return executor.submit(() -> {
            try {
                run(serverDTO, tableInfo, columns, resultCallback);
            } catch (Exception e) {

            }
        });
    }

    public void run(MySQLServerDTO serverDTO, TaskDTO.Table tableInfo, List<String> columns, ResultCallback resultCallback) throws SQLException {
        Connection conn = mySQLConnectionPool.getConnection(serverDTO);
        Statement st = conn.createStatement();

        String sql = String.format("SELECT %s FROM %s;", MySQLStringUtil.columnListToString(columns), tableInfo.getDatabase() + "." + tableInfo.getTable());
        ResultSet result = st.executeQuery(sql);

        while (result.next()) {
            Map<String, Object> data = new HashMap<>();
            for (int i = 0; i < columns.size(); i++) {
                String column = columns.get(i);
                data.put(column, result.getObject(i + 1));
            }

            resultCallback.send(data);
        }
    }

    public interface ResultCallback {
        void send(Map<String, Object> result);
    }
}
