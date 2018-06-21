package com.jhl.mds.dao.repositories;

import com.jhl.mds.dao.entities.MysqlServer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MysqlServerRepository extends JpaRepository<MysqlServer, Integer> {
}
