package com.jhl.mds.services.mysql;

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
    private MySQLWhereService mySQLWhereService;

    public MySQLDeleteService(
            MySQLConnectionPool mySQLConnectionPool,
            MySQLWhereService mySQLWhereService
    ) {
        this.mySQLConnectionPool = mySQLConnectionPool;
        this.mySQLWhereService = mySQLWhereService;
    }

    @Override
    public void execute(MySQL2MySQLMigrationDTO context, Map<String, Object> input, Consumer<Long> next, Consumer<Exception> errorHandler) throws Exception {
        TableInfoDTO tableInfo = context.getTarget();

        Connection conn = mySQLConnectionPool.getConnection(tableInfo.getServer());
        Statement st = conn.createStatement();

        String wherePart = mySQLWhereService.build(input);

        String sql = String.format("DELETE FROM %s.%s WHERE %s", tableInfo.getDatabase(), tableInfo.getTable(), wherePart);
        log.info("Run query: " + sql);

        st.execute(sql);
        st.close();

        next.accept(1L);
    }
}
