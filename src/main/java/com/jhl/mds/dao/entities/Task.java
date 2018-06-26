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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int taskId;
    private String name;
    private int fkSourceServer;
    private String sourceDatabse;
    private String sourceTable;
    private int fkTargetServer;
    private String targetDatabase;
    private String targetTable;
    private Date createdAt;
    private Date updatedAt;
}
