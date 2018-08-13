package com.jhl.mds.dao.repositories;

import com.jhl.mds.dao.entities.TaskStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.Date;

public interface TaskStatisticsRepository extends JpaRepository<TaskStatistics, Integer> {

    @Modifying
    @Query(value = "REPLACE INTO task_statistics (fk_task_id, insert_count, update_count, delete_count, created_at, updated_at) " +
            "SELECT fk_task_id, insert_count, update_count, delete_count, created_at, updated_at FROM (" +
            "   SELECT ?1 AS fk_task_id, (insert_count + ?2) AS insert_count, (update_count + ?3) AS update_count, (delete_count + ?4) AS delete_count, created_at, ?5 AS updated_at, 1 as type " +
            "   FROM task_statistics WHERE fk_task_id = ?1 " +
            "   UNION SELECT ?1, ?2, ?3, ?4, ?5, ?5, 2 " +
            ") ORDER BY type ASC LIMIT 1", nativeQuery = true)
    @Transactional
    void updateStatistics(int taskId, int insertDelta, int updateDelta, int deleteDelta, Date date);

    TaskStatistics findByFkTaskId(int taksId);
}
