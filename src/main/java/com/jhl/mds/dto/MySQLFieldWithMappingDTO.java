package com.jhl.mds.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MySQLFieldWithMappingDTO {
    private String sourceField;
    private String targetField;
    private boolean mappable;
}
