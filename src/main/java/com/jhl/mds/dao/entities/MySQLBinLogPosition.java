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

@Entity(name = "mysql_binlog_position")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MySQLBinLogPosition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int mysqlBinlogPositionId;
    private String host;
    private String port;
    private String filename;
    private long position;
    private Date createdAt;
    private Date updatedAt;
}
