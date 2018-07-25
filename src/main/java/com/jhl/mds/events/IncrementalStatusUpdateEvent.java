package com.jhl.mds.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class IncrementalStatusUpdateEvent {

    private int taskId;
    private boolean running;
}
