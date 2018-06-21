package com.jhl.mds.dto;

import lombok.Data;

@Data
public class MysqlServerDTO {
    private int serverId;
    private String name;
    private String host;
    private String port;
    private String username;
    private String password;
}
