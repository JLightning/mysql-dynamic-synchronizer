package com.jhl.mds.services.mysql.binlog;

import com.github.shyiko.mysql.binlog.event.DeleteRowsEventData;
import com.github.shyiko.mysql.binlog.event.UpdateRowsEventData;
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;

public interface MySQLBinLogListener {

    default void insert(WriteRowsEventData eventData) {
    }

    default void update(UpdateRowsEventData eventData) {
    }

    default void delete(DeleteRowsEventData deleteRowsEventData) {

    }
}
