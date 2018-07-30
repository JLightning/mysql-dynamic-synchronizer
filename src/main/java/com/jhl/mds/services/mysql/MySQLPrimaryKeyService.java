package com.jhl.mds.services.mysql;

import com.jhl.mds.dto.MySQLFieldDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MySQLPrimaryKeyService {

    public Object getPrimaryKeyValue(Map<String, Object> data, List<MySQLFieldDTO> fields) {
        for (MySQLFieldDTO field : fields) {
            if (field.getKey().equals("PRI")) {
                return data.get(field.getField());
            }
        }
        throw new RuntimeException("Cannot find PRIMARY key");
    }
}
