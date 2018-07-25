package com.jhl.mds.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ProgressUpdateEvent<T> {

    private T dto;
    private double progress;
    private boolean running;
}
