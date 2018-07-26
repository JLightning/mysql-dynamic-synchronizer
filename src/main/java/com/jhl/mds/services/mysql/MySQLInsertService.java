package com.jhl.mds.services.mysql;

import com.jhl.mds.dto.FullMigrationDTO;
import com.jhl.mds.dto.TableInfoDTO;
import com.jhl.mds.util.MySQLStringUtil;
import com.jhl.mds.util.pipeline.PipeLineTaskRunner;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.function.Consumer;

@Service
public class MySQLInsertService implements PipeLineTaskRunner<FullMigrationDTO, List<String>, Long> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private MySQLConnectionPool mySQLConnectionPool;

    @Autowired
    public MySQLInsertService(MySQLConnectionPool mySQLConnectionPool) {
        this.mySQLConnectionPool = mySQLConnectionPool;
    }

    @Override
    public void execute(FullMigrationDTO context, List<String> input, Consumer<Long> next, Consumer<Exception> errorHandler) {
        this.run(context, next, errorHandler, input);
    }

    private void run(FullMigrationDTO context, Consumer<Long> next, Consumer<Exception> errorHandler, List<String> input) {
        TableInfoDTO tableInfo = context.getTarget();

        List<String> columns = context.getTargetColumns();
        StringBuilder insertDataStrBuilder = new StringBuilder();

        for (String writeInfo : input) {
            if (insertDataStrBuilder.length() != 0) insertDataStrBuilder.append(", ");
            insertDataStrBuilder.append(writeInfo);
        }

        try {
            Connection conn = mySQLConnectionPool.getConnection(tableInfo.getServer());
            Statement st = conn.createStatement();

            String sql = String.format("INSERT IGNORE INTO %s(%s) VALUES %s;", tableInfo.getDatabase() + "." + tableInfo.getTable(), MySQLStringUtil.columnListToString(columns), insertDataStrBuilder.toString());
//            logger.info("Run query: " + sql);
            logger.info(String.format("Inserted %d rows to %s.%s", input.size(), tableInfo.getDatabase(), tableInfo.getTable()));

            st.execute(sql);
            st.close();

            next.accept((long) input.size());

        } catch (Exception e) {
            logger.error(String.format("Error when inserting %d rows to %s.%s: %s", input.size(), tableInfo.getDatabase(), tableInfo.getTable(), ExceptionUtils.getStackTrace(e)));
            errorHandler.accept(new WriteServiceException(e, input.size()));
        }
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
