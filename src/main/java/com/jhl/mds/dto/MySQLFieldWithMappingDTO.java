package com.jhl.mds.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MySQLFieldWithMappingDTO {
    private MySQLFieldDTO sourceField;
    private MySQLFieldDTO targetField;
    private boolean mappable;
}
