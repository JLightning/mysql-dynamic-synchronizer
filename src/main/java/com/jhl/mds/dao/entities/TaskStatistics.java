package com.jhl.mds.dao.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatistics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int TaskStatistics;
    private int fkTaskId;
    private long insertCount;
    private long updateCount;
    private long deleteCount;
    private Date createdAt;
    private Date updatedAt;
}
