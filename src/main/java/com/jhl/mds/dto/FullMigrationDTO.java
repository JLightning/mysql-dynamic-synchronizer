package com.jhl.mds.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FullMigrationDTO {

    private MySQLServerDTO source;
    private MySQLServerDTO target;
    private TaskDTO taskDTO;
}
