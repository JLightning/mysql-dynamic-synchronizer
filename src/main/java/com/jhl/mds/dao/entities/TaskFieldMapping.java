package com.jhl.mds.dao.entities;

import lombok.*;

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
public class TaskFieldMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int mappingId;
    private int fkTaskId;
    private String sourceField;
    private String targetField;
    private Date createdAt;
    private Date updatedAt;
}
