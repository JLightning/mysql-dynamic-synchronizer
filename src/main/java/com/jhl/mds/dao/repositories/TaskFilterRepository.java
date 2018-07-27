package com.jhl.mds.dao.repositories;

import com.jhl.mds.dao.entities.TaskFilter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskFilterRepository extends JpaRepository<TaskFilter, Integer> {

    List<TaskFilter> findByFkTaskId(int fkTaskId);

    @Query("SELECT t.filter FROM TaskFilter t where t.fkTaskId = :id")
    List<String> findFilterByTaskId(@Param("id") int id);
}
