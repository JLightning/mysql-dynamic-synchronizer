package com.jhl.mds.services.mysql;

import com.jhl.mds.dto.MySQLFieldDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MySQLPrimaryKeyService {

    public MySQLFieldDTO getPrimaryField(List<MySQLFieldDTO> fields) {
        for (MySQLFieldDTO field : fields) {
            if (field.getKey().equals("PRI")) {
                return field;
            }
        }
        throw new RuntimeException("Cannot find PRIMARY key");
    }

    public Object getPrimaryKeyValue(Map<String, Object> data, List<MySQLFieldDTO> fields) {
        MySQLFieldDTO primaryField = getPrimaryField(fields);
        return data.get(primaryField.getField());
    }
}
