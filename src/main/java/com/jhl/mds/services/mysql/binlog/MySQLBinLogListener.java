package com.jhl.mds.services.mysql.binlog;

import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;

public interface MySQLBinLogListener {
    void insert(WriteRowsEventData eventData);
}
