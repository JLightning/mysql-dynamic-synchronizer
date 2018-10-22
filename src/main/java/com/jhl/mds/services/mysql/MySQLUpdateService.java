package com.jhl.mds.services.mysql;

import com.jhl.dds.querybuilder.QueryBuilder;
import com.jhl.mds.dto.PairOfMap;
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
public class MySQLUpdateService implements PipeLineTaskRunner<MySQL2MySQLMigrationDTO, PairOfMap, Long> {

    private MySQLConnectionPool mySQLConnectionPool;

    public MySQLUpdateService(MySQLConnectionPool mySQLConnectionPool) {
        this.mySQLConnectionPool = mySQLConnectionPool;
    }

    @Override
    public void execute(MySQL2MySQLMigrationDTO context, PairOfMap input, Consumer<Long> next, Consumer<Exception> errorHandler) throws Exception {
        TableInfoDTO tableInfo = context.getTarget();
        try {
            Connection conn = mySQLConnectionPool.getConnection(tableInfo.getServer());
            Statement st = conn.createStatement();

            Map<String, Object> key = input.getFirst();
            Map<String, Object> value = input.getSecond();

            String sql  = new QueryBuilder().update(tableInfo.getDatabase(), tableInfo.getTable())
                    .set(value)
                    .where(key)
                    .build();

            log.info("Run query: " + sql);

            st.execute(sql);
            st.close();

            next.accept(1L);
        } catch (Exception e) {
            e.printStackTrace();
//            errorHandler.accept(new MySQLWriteService.WriteServiceException(e, tmpWriteList.size()));
        }
    }
}
