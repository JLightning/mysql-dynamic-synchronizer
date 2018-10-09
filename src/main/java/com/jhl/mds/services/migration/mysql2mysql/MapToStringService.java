package com.jhl.mds.services.migration.mysql2mysql;

import com.jhl.mds.util.MySQLStringUtil;
import com.jhl.mds.util.pipeline.PipeLineTaskRunner;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Consumer;

@Service
public class MapToStringService implements PipeLineTaskRunner<Object, Map<String, Object>, String> {

    @Override
    public void execute(Object context, Map<String, Object> input, Consumer<String> next, Consumer<Exception> errorHandler) throws Exception {
        next.accept(MySQLStringUtil.valueListString(input.values()));
    }
}
