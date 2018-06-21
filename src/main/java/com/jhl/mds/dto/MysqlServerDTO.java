package com.jhl.mds.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MysqlServerDTO {
    private int serverId;
    private String name;
    private String host;
    private String port;
    private String username;
    private String password;
}
