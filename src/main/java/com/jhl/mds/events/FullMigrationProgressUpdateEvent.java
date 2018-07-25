package com.jhl.mds.events;

import com.jhl.mds.dto.FullMigrationDTO;

public class FullMigrationProgressUpdateEvent extends ProgressUpdateEvent<FullMigrationDTO> {

    public FullMigrationProgressUpdateEvent(FullMigrationDTO dto, double progress, boolean running) {
        super(dto, progress, running);
    }
}
