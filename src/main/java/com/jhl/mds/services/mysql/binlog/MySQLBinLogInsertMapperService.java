package com.jhl.mds.services.mysql.binlog;

import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import com.jhl.mds.dto.MigrationDTO;
import com.jhl.mds.dto.MySQLFieldDTO;
import com.jhl.mds.dto.TableInfoDTO;
import com.jhl.mds.services.mysql.MySQLDescribeService;
import com.jhl.mds.util.pipeline.PipeLineTaskRunner;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;

@Service
public class MySQLBinLogInsertMapperService implements PipeLineTaskRunner<MigrationDTO, WriteRowsEventData, Map<String, Object>> {

    private MySQLDescribeService mySQLDescribeService;

    public MySQLBinLogInsertMapperService(MySQLDescribeService mySQLDescribeService) {
        this.mySQLDescribeService = mySQLDescribeService;
    }

    @Override
    public void execute(MigrationDTO context, WriteRowsEventData eventData, Consumer<Map<String, Object>> next, Consumer<Exception> errorHandler) throws Exception {
        List<Map<String, Object>> list = mapInsertDataToField(context.getSource(), eventData);
        for (Map<String, Object> item : list) {
            next.accept(item);
        }
    }

    public List<Map<String, Object>> mapInsertDataToField(TableInfoDTO tableInfo, WriteRowsEventData eventData) throws SQLException {
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
