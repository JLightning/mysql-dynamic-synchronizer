package com.jhl.mds.services.mysql;

import com.jhl.mds.dto.TableInfoDTO;
import com.jhl.mds.util.MySQLStringUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class MySQLWriteService {

    private static final int CHUNK_SIZE = 1000;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static ExecutorService executor = Executors.newFixedThreadPool(4);
    private MySQLConnectionPool mySQLConnectionPool;
    private final Map<TableInfoDTO, List<WriteInfo>> writeQueue = new HashMap<>();

    @Autowired
    public MySQLWriteService(MySQLConnectionPool mySQLConnectionPool) {
        this.mySQLConnectionPool = mySQLConnectionPool;
    }

    public Future<?> queue(TableInfoDTO tableInfo, WriteInfo... writeInfo) {
        synchronized (writeQueue) {
            if (!writeQueue.containsKey(tableInfo)) writeQueue.put(tableInfo, new ArrayList<>());
            writeQueue.get(tableInfo).addAll(Arrays.asList(writeInfo));
        }

        return executor.submit(() -> {
            try {
                run(tableInfo);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void run(TableInfoDTO tableInfo) throws SQLException {
        List<WriteInfo> tmpWriteList;
        synchronized (writeQueue) {
            if (!writeQueue.containsKey(tableInfo) || writeQueue.get(tableInfo).size() == 0) return;
            tmpWriteList = new ArrayList<>(writeQueue.get(tableInfo));
            writeQueue.get(tableInfo).clear();
        }

        if (tmpWriteList.size() > CHUNK_SIZE) {
            queue(tableInfo, tmpWriteList.subList(CHUNK_SIZE, tmpWriteList.size()).toArray(new WriteInfo[0]));
            tmpWriteList = tmpWriteList.subList(0, CHUNK_SIZE);
        }

        List<String> columns = tmpWriteList.get(0).getColumns();
        StringBuilder insertDataStrBuilder = new StringBuilder();

        for (WriteInfo writeInfo : tmpWriteList) {
            if (insertDataStrBuilder.length() != 0) insertDataStrBuilder.append(", ");
            insertDataStrBuilder.append(writeInfo.getInsertDatas());
        }

        Connection conn = mySQLConnectionPool.getConnection(tableInfo.getServer());
        Statement st = conn.createStatement();

        String sql = String.format("INSERT INTO %s(%s) VALUES %s;", tableInfo.getDatabase() + "." + tableInfo.getTable(), MySQLStringUtil.columnListToString(columns), insertDataStrBuilder.toString());
        logger.info("Run query: " + sql);

        st.execute(sql);
        st.close();

        for (WriteInfo writeInfo : tmpWriteList) {
            if (writeInfo.getFinishCallback() == null) continue;
            writeInfo.getFinishCallback().run();
        }
    }

    @Getter
    @AllArgsConstructor
    public static class WriteInfo {
        private List<String> columns;
        private String insertDatas;
        private Runnable finishCallback;
    }
}
