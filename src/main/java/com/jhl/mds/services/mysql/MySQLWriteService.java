package com.jhl.mds.services.mysql;

import com.jhl.mds.dto.FullMigrationDTO;
import com.jhl.mds.dto.TableInfoDTO;
import com.jhl.mds.util.MySQLStringUtil;
import com.jhl.mds.util.PipeLineTaskRunner;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
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
import java.util.function.Consumer;

@Service
public class MySQLWriteService implements PipeLineTaskRunner<FullMigrationDTO, String, Long>, PipeLineTaskRunner.SelfHandleThread {

    private static final int CHUNK_SIZE = 1000;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static ExecutorService executor = Executors.newFixedThreadPool(4);
    private MySQLConnectionPool mySQLConnectionPool;
    private final Map<TableInfoDTO, List<WriteInfo>> writeQueue = new HashMap<>();

    @Autowired
    public MySQLWriteService(MySQLConnectionPool mySQLConnectionPool) {
        this.mySQLConnectionPool = mySQLConnectionPool;
    }

    @Override
    public void queue(FullMigrationDTO context, String input, Consumer<Long> next, Consumer<Exception> errorHandler) {
        this.queue(context.getTarget(), next, errorHandler, new WriteInfo(context.getTargetColumns(), input));
    }

    public void queue(TableInfoDTO tableInfo, Consumer<Long> next, Consumer<Exception> errorHandler, WriteInfo... writeInfo) {
        synchronized (writeQueue) {
            if (!writeQueue.containsKey(tableInfo)) writeQueue.put(tableInfo, new ArrayList<>());
            writeQueue.get(tableInfo).addAll(Arrays.asList(writeInfo));
        }

        executor.submit(() -> run(tableInfo, next, errorHandler));
    }

    /**
     * TODO: what if 2 different write for same table?
     * write from writeQuene of current tableInfo to database, if queue size > CHUNK_SIZE then requeue
     *
     * @param tableInfo the info of the table to write to
     * @throws SQLException
     */
    private void run(TableInfoDTO tableInfo, Consumer<Long> next, Consumer<Exception> errorHandler) {
        List<WriteInfo> tmpWriteList;
        synchronized (writeQueue) {
            if (!writeQueue.containsKey(tableInfo) || writeQueue.get(tableInfo).size() == 0) return;
            tmpWriteList = new ArrayList<>(writeQueue.get(tableInfo));
            writeQueue.get(tableInfo).clear();
        }

        if (tmpWriteList.size() > CHUNK_SIZE) {
            queue(tableInfo, next, errorHandler, tmpWriteList.subList(CHUNK_SIZE, tmpWriteList.size()).toArray(new WriteInfo[0]));
            tmpWriteList = tmpWriteList.subList(0, CHUNK_SIZE);
        }

        List<String> columns = tmpWriteList.get(0).getColumns();
        StringBuilder insertDataStrBuilder = new StringBuilder();

        for (WriteInfo writeInfo : tmpWriteList) {
            if (insertDataStrBuilder.length() != 0) insertDataStrBuilder.append(", ");
            insertDataStrBuilder.append(writeInfo.getInsertDatas());
        }

        try {
            Connection conn = mySQLConnectionPool.getConnection(tableInfo.getServer());
            Statement st = conn.createStatement();

            String sql = String.format("INSERT INTO %s(%s) VALUES %s;", tableInfo.getDatabase() + "." + tableInfo.getTable(), MySQLStringUtil.columnListToString(columns), insertDataStrBuilder.toString());
            logger.info("Run query: " + sql);

            st.execute(sql);
            st.close();
        } catch (Exception e) {
            errorHandler.accept(new WriteServiceException(e, tmpWriteList.size()));
        }

        next.accept((long) tmpWriteList.size());
    }

    @Getter
    @AllArgsConstructor
    public static class WriteInfo {
        private List<String> columns;
        private String insertDatas;
    }

    public class WriteServiceException extends Exception {
        @Getter
        @Setter
        private long count;

        public WriteServiceException(Throwable cause, long count) {
            super(cause);
            this.count = count;
        }
    }
}
