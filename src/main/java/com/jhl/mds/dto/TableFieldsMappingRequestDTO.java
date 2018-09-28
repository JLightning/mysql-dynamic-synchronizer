package com.jhl.mds.dto;

import com.jhl.mds.jsclientgenerator.JsClientDTO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsClientDTO(fileName = "table-fields-mapping-request-dto", className = "TableFieldsMappingRequestDTO")
public class TableFieldsMappingRequestDTO {

    private int sourceServerId;
    private String sourceDatabase;
    private String sourceTable;
    private int targetServerId;
    private String targetDatabase;
    private String targetTable;

    private List<SimpleFieldMappingDTO> mapping;
}
