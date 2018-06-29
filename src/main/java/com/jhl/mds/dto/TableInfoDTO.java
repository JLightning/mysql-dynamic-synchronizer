package com.jhl.mds.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class TableInfoDTO {

    private MySQLServerDTO server;
    private String database;
    private String table;
}
