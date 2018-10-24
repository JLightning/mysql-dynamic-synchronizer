package com.jhl.mds.services.mysql.binlog;

import com.github.shyiko.mysql.binlog.event.DeleteRowsEventData;
import com.github.shyiko.mysql.binlog.event.UpdateRowsEventData;
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;

public interface MySQLBinLogListener {

    default void onInsert(WriteRowsEventData eventData) {
    }

    default void onUpdate(UpdateRowsEventData eventData) {
    }

    default void onDelete(DeleteRowsEventData eventData) {
    }
}
