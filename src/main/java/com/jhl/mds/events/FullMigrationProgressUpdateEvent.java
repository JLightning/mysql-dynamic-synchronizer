package com.jhl.mds.events;

import com.jhl.mds.dto.MigrationDTO;

public class FullMigrationProgressUpdateEvent extends ProgressUpdateEvent<MigrationDTO> {

    public FullMigrationProgressUpdateEvent(MigrationDTO dto, double progress, boolean running) {
        super(dto, progress, running);
    }
}
