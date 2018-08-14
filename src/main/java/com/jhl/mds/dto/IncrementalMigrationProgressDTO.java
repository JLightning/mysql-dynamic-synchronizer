package com.jhl.mds.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class IncrementalMigrationProgressDTO {
    private boolean running;
    private Long insertCount;
    private Long updateCount;
    private Long deleteCount;
    private boolean isDelta;
}
