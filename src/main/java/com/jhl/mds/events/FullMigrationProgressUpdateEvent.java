package com.jhl.mds.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FullMigrationProgressUpdateEvent {

    public int taskId;
    public double progress;
}
