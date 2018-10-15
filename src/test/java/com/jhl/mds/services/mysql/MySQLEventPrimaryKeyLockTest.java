package com.jhl.mds.services.mysql;

import com.jhl.mds.BaseTest;
import com.jhl.mds.dto.TableInfoDTO;
import com.jhl.mds.dto.migration.MySQLSourceMigrationDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class MySQLEventPrimaryKeyLockTest extends BaseTest {

    @Autowired
    private MySQLEventPrimaryKeyLock mySQLEventPrimaryKeyLock;

    @Test
    public void lockTest() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(200);

        AtomicBoolean isWorking = new AtomicBoolean();

        for (int i = 0; i < 200; i++) {
            final int tmpI = i;
            executor.submit(() -> {
                try {
                    MySQLSourceMigrationDTO mySQLSourceMigrationDTO = new MySQLSourceMigrationDTO() {

                        @Override
                        public int getTaskId() {
                            return 1;
                        }

                        @Override
                        public TableInfoDTO getSource() {
                            return null;
                        }
                    };
                    Object lock = mySQLEventPrimaryKeyLock.lock(mySQLSourceMigrationDTO, 1);

                    Assert.assertFalse(isWorking.get());
                    isWorking.set(true);
                    Thread.sleep(10);
                    isWorking.set(false);

                    mySQLEventPrimaryKeyLock.unlock(mySQLSourceMigrationDTO, Collections.singletonList(lock));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        Thread.sleep(1000);
    }
}
