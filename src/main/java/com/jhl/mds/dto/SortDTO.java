package com.jhl.mds.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SortDTO {

    private String key;
    private Direction direction;

    public enum Direction {
        ASC,
        DESC
    }
}
