package com.jhl.mds.services.customefilter;

import com.jhl.mds.dto.FullMigrationDTO;
import com.jhl.mds.util.PipeLineTaskRunner;
import com.jhl.mds.util.PipelineCancelException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Service
public class CustomFilterService implements PipeLineTaskRunner<FullMigrationDTO, Map<String, Object>, Map<String, Object>> {

    private CustomFilterPool customFilterPool;

    public CustomFilterService(CustomFilterPool customFilterPool) {
        this.customFilterPool = customFilterPool;
    }

    @Override
    public void queue(FullMigrationDTO context, Map<String, Object> input, Consumer<Map<String, Object>> next, Consumer<Exception> errorHandler) throws Exception {
        List<String> filters = context.getFilters();
        if (filters != null) {
            for (String filter : filters) {
                if (!customFilterPool.resolve(filter, input).get()) throw new PipelineCancelException("Filter failed");
            }
        }

        next.accept(input);
    }
}
