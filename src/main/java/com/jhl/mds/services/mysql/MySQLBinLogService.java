package com.jhl.mds.services.mysql;

import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import com.jhl.mds.dto.MySQLFieldDTO;
import com.jhl.mds.dto.TableInfoDTO;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.*;

@Service
public class MySQLBinLogService {

    private MySQLDescribeService mySQLDescribeService;

    public MySQLBinLogService(MySQLDescribeService mySQLDescribeService) {
        this.mySQLDescribeService = mySQLDescribeService;
    }

    public List<Map<String, Object>> mapDataToField(TableInfoDTO tableInfo, WriteRowsEventData eventData) throws SQLException {
        List<MySQLFieldDTO> fields = mySQLDescribeService.getFields(tableInfo.getServer(), tableInfo.getDatabase(), tableInfo.getTable());

        List<Map<String, Object>> result = new ArrayList<>();

        BitSet includedColumns = eventData.getIncludedColumns();
        Map<Integer, MySQLFieldDTO> includedFields = new HashMap<>();
        int count = 0;
        for (int i = includedColumns.nextSetBit(0); i != -1; i = includedColumns.nextSetBit(i + 1)) {
            includedFields.put(count, fields.get(i));
            count++;
        }

        for (Serializable[] row : eventData.getRows()) {
            Map<String, Object> obj = new HashMap<>();

            for (int i = 0; i < includedFields.size(); i++) {
                obj.put(includedFields.get(i).getField(), row[i]);
            }

            result.add(obj);
        }
        return result;
    }
}
