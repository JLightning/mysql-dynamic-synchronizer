package com.jhl.mds.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FullMigrationDTO {

    private TableInfoDTO source;
    private TableInfoDTO target;
    private List<SimpleFieldMappingDTO> mapping;
}
