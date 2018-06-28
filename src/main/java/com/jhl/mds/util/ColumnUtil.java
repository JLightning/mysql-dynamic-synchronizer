package com.jhl.mds.util;

import org.apache.commons.lang3.StringUtils;

public class ColumnUtil {

    public static String columnListToString(Iterable<?> columns) {
        return "`" + StringUtils.join(columns, "`, `") + "`";
    }
}
