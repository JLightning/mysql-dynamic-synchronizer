package com.jhl.mds.dao.repositories;

import com.jhl.mds.dao.entities.TaskFieldMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface TaskFieldMappingRepository extends JpaRepository<TaskFieldMapping, Integer> {

    List<TaskFieldMapping> findByFkTaskId(int taskId);

    @Transactional
    long deleteByFkTaskId(int taskId);
}
