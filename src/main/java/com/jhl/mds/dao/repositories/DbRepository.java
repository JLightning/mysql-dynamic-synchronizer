package com.jhl.mds.dao.repositories;

import com.jhl.mds.dao.entities.Db;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DbRepository extends JpaRepository<Db, Integer> {
}
