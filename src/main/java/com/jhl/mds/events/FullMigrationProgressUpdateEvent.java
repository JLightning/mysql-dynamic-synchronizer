package com.jhl.mds.events;

import com.jhl.mds.dto.migration.MySQL2MySQLMigrationDTO;

public class FullMigrationProgressUpdateEvent extends ProgressUpdateEvent<Integer> {

    public FullMigrationProgressUpdateEvent(int taskId, double progress, boolean running) {
        super(taskId, progress, running);
    }
}
