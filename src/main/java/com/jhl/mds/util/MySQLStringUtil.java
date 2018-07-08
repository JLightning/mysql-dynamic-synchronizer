package com.jhl.mds.util;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class MySQLStringUtil {

    public static String columnListToString(Iterable<?> columns) {
        return "`" + StringUtils.join(columns, "`, `") + "`";
    }

    public static String valueListString(Iterable<?> values) {
        List<Object> list = new ArrayList<>();
        for (Object value : values) {
            if (value != null) value = "'" + value + "'";
            list.add(value);
        }
        return "(" + StringUtils.join(list, ", ") + ")";
    }
}
