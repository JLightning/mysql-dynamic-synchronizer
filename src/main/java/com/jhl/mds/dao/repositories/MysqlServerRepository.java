package com.jhl.mds.dao.repositories;

import com.jhl.mds.dao.entities.MySQLServer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MysqlServerRepository extends JpaRepository<MySQLServer, Integer> {
}
