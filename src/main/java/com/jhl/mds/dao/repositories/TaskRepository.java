package com.jhl.mds.dao.repositories;

import com.jhl.mds.dao.entities.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Integer> {

    @Modifying
    @Query(value = "UPDATE task SET full_migration_progress = ?2 WHERE task_id = ?1", nativeQuery = true)
    @Transactional
    void updateFullMigrationProgress(int taskId, double progress);

    @Modifying
    @Query(value = "UPDATE task SET incremental_migration_active = ?2 WHERE task_id = ?1", nativeQuery = true)
    @Transactional
    void updateIncrementalMigrationActive(int taskId, boolean active);

    List<Task> findByIncrementalMigrationActive(boolean active);
}
