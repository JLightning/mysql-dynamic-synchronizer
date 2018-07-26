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
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int taskId;
    private String name;
    private int fkSourceServer;
    private String sourceDatabase;
    private String sourceTable;
    private int fkTargetServer;
    private String targetDatabase;
    private String targetTable;
    private int taskType;
    private String insertType;
    private double fullMigrationProgress;
    private boolean incrementalMigrationActive;
    private Date createdAt;
    private Date updatedAt;
}
