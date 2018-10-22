package com.jhl.mds.services.migration;

import com.jhl.mds.dto.PairOfMap;
import com.jhl.mds.dto.migration.FilterableMigrationDTO;
import com.jhl.mds.services.customefilter.CustomFilterService;
import com.jhl.mds.util.pipeline.PipeLineTaskRunner;
import com.jhl.mds.util.pipeline.PipelineCancelException;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
public class InsertOrDeleteWhenUpdatingService implements PipeLineTaskRunner<FilterableMigrationDTO, PairOfMap, PairOfMap> {

    private CustomFilterService customFilterService;

    public InsertOrDeleteWhenUpdatingService(
            CustomFilterService customFilterService
    ) {
        this.customFilterService = customFilterService;
    }

    @Override
    public void execute(FilterableMigrationDTO context, PairOfMap input, Consumer<PairOfMap> next, Consumer<Exception> errorHandler) throws Exception {
        boolean needInsert = false, needDelete = false;
        try {
            customFilterService.filter(context, input.getFirst());
        } catch (PipelineCancelException e) {
            needInsert = true;
        }

        try {
            customFilterService.filter(context, input.getSecond());
        } catch (PipelineCancelException e) {
            needDelete = true;
        }

        input.setInsertNeeded(needInsert);
        input.setDeleteNeeded(needDelete);

        next.accept(input);
    }
}
