package com.jhl.mds.services.mysql;

import com.jhl.mds.dto.migration.MigrationDTO;
import com.jhl.mds.dto.MySQLFieldDTO;
import com.jhl.mds.dto.TableInfoDTO;
import com.jhl.mds.util.MySQLStringUtil;
import com.jhl.mds.util.pipeline.PipeLineTaskRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class MySQLReadService implements PipeLineTaskRunner<MigrationDTO, Object, Map<String, Object>> {

    private MySQLConnectionPool mySQLConnectionPool;
    private MySQLDescribeService mySQLDescribeService;

    @Autowired
    public MySQLReadService(MySQLConnectionPool mySQLConnectionPool, MySQLDescribeService mySQLDescribeService) {
        this.mySQLConnectionPool = mySQLConnectionPool;
        this.mySQLDescribeService = mySQLDescribeService;
    }

    @Override
    public void execute(MigrationDTO context, Object input, Consumer<Map<String, Object>> next, Consumer<Exception> errorHandler) throws SQLException {
        run(context.getSource(), next);
    }

    public void run(TableInfoDTO tableInfo, Consumer<Map<String, Object>> next) throws SQLException {
        List<MySQLFieldDTO> fields = mySQLDescribeService.getFields(tableInfo.getServer(), tableInfo.getDatabase(), tableInfo.getTable());
        List<String> columns = fields.stream().map(MySQLFieldDTO::getField).collect(Collectors.toList());

        Connection conn = mySQLConnectionPool.getConnection(tableInfo.getServer());
        Statement st = conn.createStatement();

        String sql = String.format("SELECT %s FROM %s;", MySQLStringUtil.columnListToString(columns), tableInfo.getDatabase() + "." + tableInfo.getTable());
        ResultSet result = st.executeQuery(sql);

        while (result.next()) {
            Map<String, Object> data = new HashMap<>();
            for (int i = 0; i < columns.size(); i++) {
                String column = columns.get(i);
                data.put(column, result.getObject(i + 1));
            }

            next.accept(data);
        }
    }

    public long count(TableInfoDTO tableInfo) throws SQLException {
        Connection conn = mySQLConnectionPool.getConnection(tableInfo.getServer());
        Statement st = conn.createStatement();

        String sql = String.format("SELECT COUNT(1) FROM %s;", tableInfo.getDatabase() + "." + tableInfo.getTable());
        ResultSet result = st.executeQuery(sql);

        result.next();
        return result.getLong(1);
    }
}
