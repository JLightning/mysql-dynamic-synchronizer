package com.jhl.mds;

import com.jhl.mds.dto.MySQLServerDTO;
import com.jhl.mds.services.mysql.MySQLConnectionPool;
import lombok.AccessLevel;
import lombok.Getter;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public abstract class BaseTest {

    public final static String TEST_DATABASE = "mds";
    @Autowired
    private MySQLConnectionPool mySQLConnectionPool;
    private Connection connection;
    @Getter(value = AccessLevel.PROTECTED)
    private Statement statement;
    @Getter(value = AccessLevel.PROTECTED)
    private MySQLServerDTO sourceServerDTO;
    private List<String> createdTables = new ArrayList<>();

    @Before
    public void setup() throws Exception {
        sourceServerDTO = new MySQLServerDTO(0, "test", "localhost", "3307", "root", "root");

        connection = mySQLConnectionPool.getConnection(sourceServerDTO);
        statement = connection.createStatement();
    }

    protected void checkTime(String task, Runnable r) {
        Instant start = Instant.now();
        r.run();
        System.out.println("elapsed time for `" + task + "` = " + Duration.between(start, Instant.now()));
    }

    protected String prepareTable(String template) throws Exception {
        String tableName = generateRandomTableName();

        ClassLoader classLoader = getClass().getClassLoader();
        File templateFile = new File(classLoader.getResource(template).getFile());

        BufferedReader br = new BufferedReader(new FileReader(templateFile));
        StringBuilder sqlBuilder = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sqlBuilder.append(line);
        }
        String sql = sqlBuilder.toString();
        sql = sql.replaceAll("\\{table_name}", "`" + TEST_DATABASE + "`.`" + tableName + "`");

        System.out.println("sql = " + sql);

        statement.execute(sql);
        createdTables.add(tableName);
        return tableName;
    }

    protected String generateRandomTableName() {
        return "table_" + (int) (Math.random() * 10000);
    }

    protected void addCreatedTable(String table) {
        createdTables.add(table);
    }

    @After
    public void cleanCratedTables() throws SQLException {
        for (String table : createdTables) {
            statement.execute("DROP TABLE `" + TEST_DATABASE + "`.`" + table + "`");
        }
    }
}
