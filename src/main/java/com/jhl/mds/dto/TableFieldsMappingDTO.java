package com.jhl.mds.dto;

import lombok.Data;

@Data
public class TableFieldsMappingDTO {

    private int sourceServerId;
    private String sourceDatabase;
    private String sourceTable;
    private int targetServerId;
    private String targetDatabase;
    private String targetTable;
}
