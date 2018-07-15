package com.jhl.mds.dto;

import lombok.Data;

import java.util.List;

@Data
public class TableFieldsMappingRequestDTO {

    private int sourceServerId;
    private String sourceDatabase;
    private String sourceTable;
    private int targetServerId;
    private String targetDatabase;
    private String targetTable;

    private List<SimpleFieldMappingDTO> mapping;
}
