package com.jhl.mds.dao.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatistics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int taskStatisticsId;
    private int fkTaskId;
    private long insertCount;
    private long updateCount;
    private long deleteCount;
    private double delayMs;
    @Column(name = "operation_count", insertable = false, updatable = false)
    private long operationCount;
    private Date createdAt;
    private Date updatedAt;
}
