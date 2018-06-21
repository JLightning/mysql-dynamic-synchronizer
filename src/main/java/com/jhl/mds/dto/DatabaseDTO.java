package com.jhl.mds.dto;

import lombok.Data;

@Data
public class DatabaseDTO {
    private String name;
    private String database;
    private String host;
    private String port;
    private String username;
    private String password;
}
