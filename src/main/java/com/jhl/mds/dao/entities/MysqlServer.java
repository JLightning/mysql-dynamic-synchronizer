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
public class MysqlServer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int serverId;
    private String name;
    private String host;
    private String port;
    private String username;
    private String password;
    private Date createdAt;
    private Date updatedAt;
}
