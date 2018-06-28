package com.jhl.mds.util;

import org.apache.commons.lang3.StringUtils;

public class MySQLStringUtil {

    public static String columnListToString(Iterable<?> columns) {
        return "`" + StringUtils.join(columns, "`, `") + "`";
    }

    public static String valueListString(Iterable<?> values) {
        return "('" + StringUtils.join(values, "', '") + "')";
    }
}
