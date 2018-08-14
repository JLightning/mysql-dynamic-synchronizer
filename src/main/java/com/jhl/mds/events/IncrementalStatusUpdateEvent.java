package com.jhl.mds.events;

import com.jhl.mds.dto.IncrementalMigrationProgressDTO;
import lombok.Getter;

@Getter
public class IncrementalStatusUpdateEvent extends IncrementalMigrationProgressDTO {

    private int taskId;

    public IncrementalStatusUpdateEvent(int taskId, boolean running, Long insertCount, Long updateCount, Long deleteCount, boolean isDelta) {
        super(running, insertCount, updateCount, deleteCount, isDelta);
        this.taskId = taskId;
    }
}
