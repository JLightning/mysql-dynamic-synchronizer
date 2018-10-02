package com.jhl.mds.dto;

import com.jhl.mds.jsclientgenerator.JsClientDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsClientDTO(fileName = "simple-field-mapping-dto", className = "SimpleFieldMappingDTO")
public class SimpleFieldMappingDTO {
    private String sourceField;
    private String targetField;
}
