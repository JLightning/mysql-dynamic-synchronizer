package com.jhl.mds.dto;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class IncrementalMigrationProgressDTO {
    public boolean running;
    public long insertCount;
    public long updateCount;
    public long deleteCount;
}
