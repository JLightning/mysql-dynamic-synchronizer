package com.jhl.mds.services.mysql;

import com.jhl.dds.querybuilder.QueryBuilder;
import com.jhl.mds.dto.TableInfoDTO;
import com.jhl.mds.dto.migration.MySQL2MySQLMigrationDTO;
import com.jhl.mds.util.pipeline.PipeLineTaskRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Map;
import java.util.function.Consumer;

@Service
@Slf4j
public class MySQLDeleteService implements PipeLineTaskRunner<MySQL2MySQLMigrationDTO, Map<String, Object>, Long> {

    private MySQLConnectionPool mySQLConnectionPool;

    public MySQLDeleteService(MySQLConnectionPool mySQLConnectionPool) {
        this.mySQLConnectionPool = mySQLConnectionPool;
    }

    @Override
    public void execute(MySQL2MySQLMigrationDTO context, Map<String, Object> input, Consumer<Long> next, Consumer<Exception> errorHandler) throws Exception {
        TableInfoDTO tableInfo = context.getTarget();

        Connection conn = mySQLConnectionPool.getConnection(tableInfo.getServer());
        Statement st = conn.createStatement();

        String sql = new QueryBuilder().deleteFrom(tableInfo.getDatabase(), tableInfo.getTable())
                .where(input)
                .build();

        log.info("Run query: " + sql);

        st.execute(sql);
        st.close();

        next.accept(1L);
    }
}
