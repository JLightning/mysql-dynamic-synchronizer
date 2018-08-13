package com.jhl.mds.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class IncrementalStatusUpdateEvent {

    private int taskId;
    private boolean running;
    public long insertCount;
    public long updateCount;
    public long deleteCount;
}
