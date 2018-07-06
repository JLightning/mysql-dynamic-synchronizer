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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MySQLWriteService2 {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static ExecutorService executor = Executors.newFixedThreadPool(1);
    private MySQLConnectionPool mySQLConnectionPool;
    private final Map<TableInfoDTO, List<WriteInfo>> writeQueue = new HashMap<>();
    private final AtomicInteger count = new AtomicInteger();

    @Autowired
    public MySQLWriteService2(MySQLConnectionPool mySQLConnectionPool) {
        this.mySQLConnectionPool = mySQLConnectionPool;
    }

    public Future<?> queue(TableInfoDTO tableInfo, WriteInfo writeInfo) {
        synchronized (writeQueue) {
            if (!writeQueue.containsKey(tableInfo)) writeQueue.put(tableInfo, new ArrayList<>());
            writeQueue.get(tableInfo).add(writeInfo);
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

            System.out.println("count.incrementAndGet() = " + count.addAndGet(1));
            System.out.println("writeQueue.size() = " + writeQueue.get(tableInfo).size());

            tmpWriteList = new ArrayList<>(writeQueue.get(tableInfo));
            writeQueue.get(tableInfo).clear();
        }

        List<String> columns = tmpWriteList.get(0).getColumns();
        String insertDatas = "";

        for (WriteInfo writeInfo : tmpWriteList) {
            if (!insertDatas.equals("")) insertDatas += ", ";
            insertDatas += writeInfo.getInsertDatas();
        }

        Connection conn = mySQLConnectionPool.getConnection(tableInfo.getServer());
        Statement st = conn.createStatement();

        String sql = String.format("INSERT INTO %s(%s) VALUES %s;", tableInfo.getDatabase() + "." + tableInfo.getTable(), MySQLStringUtil.columnListToString(columns), insertDatas);
        logger.info("Run query: " + sql);

        try {
            st.execute(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        st.close();

        for (WriteInfo writeInfo : tmpWriteList) {
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
