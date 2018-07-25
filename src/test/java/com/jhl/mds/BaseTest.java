package com.jhl.mds;

import com.jhl.mds.dto.MySQLServerDTO;
import com.jhl.mds.services.mysql.MySQLConnectionPool;
import lombok.AccessLevel;
import lombok.Getter;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Connection;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public abstract class BaseTest {

    @Autowired
    private MySQLConnectionPool mySQLConnectionPool;
    private Connection connection;
    @Getter(value = AccessLevel.PROTECTED)
    private Statement statement;
    @Getter(value = AccessLevel.PROTECTED)
    private MySQLServerDTO sourceServerDTO;

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
}
