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
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int taskId;
    private String taskName;
    private int fkSourceServer;
    private String sourceDatabse;
    private String sourceTable;
    private int fkTargetServer;
    private String targetDatabase;
    private String targetTable;
    private Date createdAt;
    private Date updatedAt;
}
