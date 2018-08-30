package com.jhl.mds.services.mysql;

import com.jhl.mds.dto.TableInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Service
@Slf4j
public class MySQLCommonService {

    private MySQLConnectionPool mySQLConnectionPool;

    public MySQLCommonService(MySQLConnectionPool mySQLConnectionPool) {
        this.mySQLConnectionPool = mySQLConnectionPool;
    }

    public void truncateTable(TableInfoDTO tableInfoDTO) throws SQLException {
        Connection conn = mySQLConnectionPool.getConnection(tableInfoDTO.getServer());
        Statement st = conn.createStatement();

        String sql = String.format("TRUNCATE `%s`.`%s`;", tableInfoDTO.getDatabase(), tableInfoDTO.getTable());
        st.execute(sql);

        log.info(sql);
    }
}
