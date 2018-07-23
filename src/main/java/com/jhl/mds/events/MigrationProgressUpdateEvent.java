package com.jhl.mds.events;

import com.jhl.mds.dto.FullMigrationDTO;

public class MigrationProgressUpdateEvent extends ProgressUpdateEvent<FullMigrationDTO> {

    public MigrationProgressUpdateEvent(FullMigrationDTO dto, double progress) {
        super(dto, progress);
    }
}
