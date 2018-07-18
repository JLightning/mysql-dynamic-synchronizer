package com.jhl.mds.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MySQLIndexDTO {
    private String table;
    private boolean nonUnique;
    private String keyName;
    private int seqInIndex;
    private String columnName;
    private String collation; // A: Ascending, D: Descending, null: none
    private long cardinality;
    private String subPart;
    private String packed;
    private boolean isNull;
    private String indexType;
    private String comment;
    private String indexComment;
}
