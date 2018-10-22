package com.jhl.mds.services.task;

import com.jhl.mds.dao.repositories.TaskRepository;
import com.jhl.mds.dao.repositories.TaskStatisticsRepository;
import com.jhl.mds.events.FullMigrationProgressUpdateEvent;
import com.jhl.mds.events.IncrementalStatusUpdateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class TaskStatisticService {

    private ApplicationEventPublisher eventPublisher;
    private TaskRepository taskRepository;
    private TaskStatisticsRepository taskStatisticsRepository;

    public TaskStatisticService(
            ApplicationEventPublisher eventPublisher,
            TaskRepository taskRepository,
            TaskStatisticsRepository taskStatisticsRepository
    ) {
        this.eventPublisher = eventPublisher;
        this.taskRepository = taskRepository;
        this.taskStatisticsRepository = taskStatisticsRepository;
    }

    public void updateTaskIncrementalStatistic(int taskId, long insertDelta, long updateDelta, long deleteDelta) {
        try {
            taskStatisticsRepository.updateStatistics(taskId, insertDelta, updateDelta, deleteDelta, new Date());
            eventPublisher.publishEvent(new IncrementalStatusUpdateEvent(taskId, true, insertDelta, updateDelta, 0L, true));
        } catch (DataIntegrityViolationException e) {
            log.error(String.format("Task %d doesn't exist, cannot update statistics", taskId));
        }
    }

    public void updateTaskFullMigrationProgress(int taskId, double progress) {
        eventPublisher.publishEvent(new FullMigrationProgressUpdateEvent(taskId, progress, progress != 100));
        taskRepository.updateFullMigrationProgress(taskId, Math.round(progress));
    }
}
