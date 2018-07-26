package com.jhl.mds.services.mysql.binlog;

import com.github.shyiko.mysql.binlog.event.UpdateRowsEventData;
import com.jhl.mds.dto.MigrationDTO;
import com.jhl.mds.dto.MySQLFieldDTO;
import com.jhl.mds.dto.TableInfoDTO;
import com.jhl.mds.services.mysql.MySQLDescribeService;
import com.jhl.mds.util.pipeline.PipeLineTaskRunner;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;

@Service
public class MySQLBinLogUpdateMapperService implements PipeLineTaskRunner<MigrationDTO, UpdateRowsEventData, Pair<Map<String, Object>, Map<String, Object>>> {

    private MySQLDescribeService mySQLDescribeService;

    public MySQLBinLogUpdateMapperService(MySQLDescribeService mySQLDescribeService) {
        this.mySQLDescribeService = mySQLDescribeService;
    }

    @Override
    public void execute(MigrationDTO context, UpdateRowsEventData eventData, Consumer<Pair<Map<String, Object>, Map<String, Object>>> next, Consumer<Exception> errorHandler) throws Exception {
        List<Pair<Map<String, Object>, Map<String, Object>>> list = mapUpdateDataToField(context.getSource(), eventData);
        for (Pair<Map<String, Object>, Map<String, Object>> item : list) {
            next.accept(item);
        }
    }


    public List<Pair<Map<String, Object>, Map<String, Object>>> mapUpdateDataToField(TableInfoDTO tableInfo, UpdateRowsEventData eventData) throws SQLException {
        List<MySQLFieldDTO> fields = mySQLDescribeService.getFields(tableInfo.getServer(), tableInfo.getDatabase(), tableInfo.getTable());

        List<Pair<Map<String, Object>, Map<String, Object>>> result = new ArrayList<>();

        BitSet includedColumns = eventData.getIncludedColumns();
        Map<Integer, MySQLFieldDTO> includedFields = new HashMap<>();
        int count = 0;
        for (int i = includedColumns.nextSetBit(0); i != -1; i = includedColumns.nextSetBit(i + 1)) {
            includedFields.put(count, fields.get(i));
            count++;
        }

        for (Map.Entry<Serializable[], Serializable[]> entry : eventData.getRows()) {
            Serializable[] key = entry.getKey();
            Serializable[] value = entry.getValue();
            Map<String, Object> keyObj = new HashMap<>();
            Map<String, Object> valueObj = new HashMap<>();

            for (int i = 0; i < includedFields.size(); i++) {
                keyObj.put(includedFields.get(i).getField(), key[i]);
            }

            for (int i = 0; i < includedFields.size(); i++) {
                valueObj.put(includedFields.get(i).getField(), value[i]);
            }

            result.add(Pair.of(keyObj, valueObj));
        }
        return result;
    }
}
