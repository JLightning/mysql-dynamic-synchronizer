package com.jhl.mds.services.customefilter;

import com.jhl.mds.dto.migration.FilterableMigrationDTO;
import com.jhl.mds.dto.migration.MySQL2MySQLMigrationDTO;
import com.jhl.mds.util.pipeline.PipeLineTaskRunner;
import com.jhl.mds.util.pipeline.PipelineCancelException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Service
public class CustomFilterService implements PipeLineTaskRunner<FilterableMigrationDTO, Map<String, Object>, Map<String, Object>> {

    private CustomFilterPool customFilterPool;

    public CustomFilterService(CustomFilterPool customFilterPool) {
        this.customFilterPool = customFilterPool;
    }

    @Override
    public void execute(FilterableMigrationDTO context, Map<String, Object> input, Consumer<Map<String, Object>> next, Consumer<Exception> errorHandler) throws Exception {
        filter(context, input);
        next.accept(input);
    }

    public void filter(FilterableMigrationDTO context, Map<String, Object> input) throws InterruptedException, java.util.concurrent.ExecutionException {
        List<String> filters = context.getFilters();
        if (filters != null) {
            for (String filter : filters) {
                if (!customFilterPool.resolve(filter, input).get()) throw new PipelineCancelException("Filter failed");
            }
        }
    }
}
