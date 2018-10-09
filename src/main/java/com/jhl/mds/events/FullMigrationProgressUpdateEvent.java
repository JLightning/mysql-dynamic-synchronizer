package com.jhl.mds.events;

import com.jhl.mds.dto.migration.MySQL2MySQLMigrationDTO;

public class FullMigrationProgressUpdateEvent extends ProgressUpdateEvent<MySQL2MySQLMigrationDTO> {

    public FullMigrationProgressUpdateEvent(MySQL2MySQLMigrationDTO dto, double progress, boolean running) {
        super(dto, progress, running);
    }
}
