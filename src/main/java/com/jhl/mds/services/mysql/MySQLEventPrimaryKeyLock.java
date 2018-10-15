package com.jhl.mds.services.mysql;

import com.jhl.mds.dto.migration.MySQLSourceMigrationDTO;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.*;

// TODO: fix the lock for multiple insert on same primary key, support no primary key
@Service
public class MySQLEventPrimaryKeyLock {

    private final Map<Integer, Set<Object>> primaryKeyLock = new HashMap<>();
    private MySQLDescribeService mySQLDescribeService;
    private MySQLPrimaryKeyService mySQLPrimaryKeyService;

    public MySQLEventPrimaryKeyLock(
            MySQLDescribeService mySQLDescribeService,
            MySQLPrimaryKeyService mySQLPrimaryKeyService
    ) {
        this.mySQLDescribeService = mySQLDescribeService;
        this.mySQLPrimaryKeyService = mySQLPrimaryKeyService;
    }

    public Object lock(MySQLSourceMigrationDTO context, Object lockKey) throws InterruptedException {
        final Set<Object> lock = getPrimaryKeyLock(context.getTaskId());
        synchronized (lock) {
            while (lock.contains(lockKey)) {
                lock.wait();
            }

            lock.add(lockKey);
        }

        return lockKey;
    }

    public Object lock(MySQLSourceMigrationDTO context, Map<String, Object> data) throws InterruptedException, SQLException {
        Object primaryKeyValue = mySQLPrimaryKeyService.getPrimaryKeyValue(data, mySQLDescribeService.getFields(context.getSource()));
        return lock(context, primaryKeyValue);
    }

    public void unlock(MySQLSourceMigrationDTO context, Collection<?> primaryKeyValue) {
        final Set<Object> lock = getPrimaryKeyLock(context.getTaskId());
        synchronized (lock) {
            lock.removeAll(primaryKeyValue);
            lock.notifyAll();
        }
    }

    private synchronized Set<Object> getPrimaryKeyLock(int taskId) {
        if (!primaryKeyLock.containsKey(taskId)) primaryKeyLock.put(taskId, new HashSet<>());
        return primaryKeyLock.get(taskId);
    }
}
