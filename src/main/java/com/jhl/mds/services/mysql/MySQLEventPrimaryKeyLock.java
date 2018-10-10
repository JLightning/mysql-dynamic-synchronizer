package com.jhl.mds.services.mysql;

import com.jhl.mds.dto.migration.MySQLSourceMigrationDTO;
import org.springframework.stereotype.Service;

import java.util.*;

// TODO: fix the lock for multiple insert on same primary key, support no primary key
@Service
public class MySQLEventPrimaryKeyLock {

    private MySQLDescribeService mySQLDescribeService;
    private MySQLPrimaryKeyService mySQLPrimaryKeyService;
    private final Map<Integer, Set<Object>> primaryKeyLock = new HashMap<>();

    public MySQLEventPrimaryKeyLock(
            MySQLDescribeService mySQLDescribeService,
            MySQLPrimaryKeyService mySQLPrimaryKeyService
    ) {
        this.mySQLDescribeService = mySQLDescribeService;
        this.mySQLPrimaryKeyService = mySQLPrimaryKeyService;
    }

    public Object lock(MySQLSourceMigrationDTO context, Map<String, Object> data) throws Exception {
        Object primaryKeyValue = mySQLPrimaryKeyService.getPrimaryKeyValue(data, mySQLDescribeService.getFields(context.getSource()));
        final Set<Object> lock = getPrimaryKeyLock(context.getTaskId());
        synchronized (lock) {
            while (lock.contains(primaryKeyValue)) {
                lock.wait();
            }

            lock.add(primaryKeyValue);
        }

        return primaryKeyValue;
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
