package com.jhl.mds.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MySQLFieldDTO {
    private String field;
    private String type;
    private boolean nullable;
    private String key;
    private String defaultValue;
    private String extra;
}
