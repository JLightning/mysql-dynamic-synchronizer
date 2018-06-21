package com.jhl.mds.dao.entities;

import lombok.Builder;
import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Builder
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int taskId;
    private String taskName;
    private String taskCode;
    private int fkSourceDatabase;
    private String sourceTable;
    private int fkTargetDatabase;
    private String targetTable;
    private Date createdAt;
    private Date updatedAt;
}
