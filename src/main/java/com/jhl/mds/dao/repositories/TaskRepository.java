package com.jhl.mds.dao.repositories;

import com.jhl.mds.dao.entities.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Integer> {

}
