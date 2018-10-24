package com.jhl.mds.dao.repositories;

import com.jhl.mds.dao.entities.TaskStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.Date;

public interface TaskStatisticsRepository extends JpaRepository<TaskStatistics, Integer> {

    @Modifying
    @Query(value = "REPLACE INTO task_statistics (task_statistics_id, fk_task_id, insert_count, update_count, delete_count, delay_ms, max_delay_ms, min_delay_ms, created_at, updated_at) " +
            "SELECT task_statistics_id, fk_task_id, insert_count, update_count, delete_count, delay_ms, max_delay_ms, min_delay_ms, created_at, updated_at FROM (" +
            "   SELECT task_statistics_id, ?1 AS fk_task_id, (insert_count + ?2) AS insert_count, (update_count + ?3) AS update_count, (delete_count + ?4) AS delete_count, created_at, ?5 AS updated_at," +
            "   (delay_ms * operation_count + ?6) / (operation_count + 1) AS delay_ms, GREATEST(max_delay_ms, ?6) AS max_delay_ms, LEAST(min_delay_ms, ?6) AS min_delay_ms, 1 AS type " +
            "   FROM task_statistics WHERE fk_task_id = ?1 " +
            "   UNION SELECT NULL, ?1, ?2, ?3, ?4, ?5, ?5, ?6, ?6, ?6, 2 " +
            ") AS sub_table ORDER BY sub_table.type ASC LIMIT 1", nativeQuery = true)
    @Transactional
    void updateStatistics(int taskId, long insertDelta, long updateDelta, long deleteDelta, Date date, double delayMs);

    TaskStatistics findByFkTaskId(int taksId);
}
