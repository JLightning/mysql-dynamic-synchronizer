package com.jhl.mds.dto;

import com.jhl.mds.jsclientgenerator.JsClientDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MySQLFieldDTO {
    private String field;
    private String type;
    private String collation;
    private boolean nullable;
    private String key;
    private String defaultValue;
    private String extra;
    private String comment;
}
