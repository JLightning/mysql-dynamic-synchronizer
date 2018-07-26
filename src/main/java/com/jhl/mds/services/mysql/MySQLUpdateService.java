package com.jhl.mds.services.mysql;

import com.jhl.mds.dto.MigrationDTO;
import com.jhl.mds.dto.TableInfoDTO;
import com.jhl.mds.util.pipeline.PipeLineTaskRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Map;
import java.util.function.Consumer;

@Service
public class MySQLUpdateService implements PipeLineTaskRunner<MigrationDTO, Pair<Map<String, Object>, Map<String, Object>>, Long> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private MySQLConnectionPool mySQLConnectionPool;

    public MySQLUpdateService(MySQLConnectionPool mySQLConnectionPool) {
        this.mySQLConnectionPool = mySQLConnectionPool;
    }

    @Override
    public void execute(MigrationDTO context, Pair<Map<String, Object>, Map<String, Object>> input, Consumer<Long> next, Consumer<Exception> errorHandler) throws Exception {
        TableInfoDTO tableInfo = context.getTarget();
        try {
            Connection conn = mySQLConnectionPool.getConnection(tableInfo.getServer());
            Statement st = conn.createStatement();

            Map<String, Object> key = input.getFirst();
            Map<String, Object> value = input.getSecond();

            StringBuilder setPart = new StringBuilder();
            for (Map.Entry<String, Object> e : value.entrySet()) {
                if (setPart.length() > 0) setPart.append(", ");
                setPart.append(e.getKey()).append(" = ").append(e.getValue());
            }

            StringBuilder wherePart = new StringBuilder();
            for (Map.Entry<String, Object> e : key.entrySet()) {
                if (wherePart.length() > 0) wherePart.append(" AND ");
                wherePart.append(e.getKey()).append(" = ").append(e.getValue());
            }

            String sql = String.format("UPDATE %s.%s SET %s WHERE %s", tableInfo.getDatabase(), tableInfo.getTable(), setPart, wherePart);
            logger.info("Run query: " + sql);

            st.execute(sql);
            st.close();

            next.accept(1L);
        } catch (Exception e) {
            e.printStackTrace();
//            errorHandler.accept(new MySQLWriteService.WriteServiceException(e, tmpWriteList.size()));
        }
    }
}
