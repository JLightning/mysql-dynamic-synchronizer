package com.jhl.mds.dto.migration;

import com.jhl.mds.dto.TableInfoDTO;

public interface MySQLSourceMigrationDTO {

    int getTaskId();
    TableInfoDTO getSource();
}
