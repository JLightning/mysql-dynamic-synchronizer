package com.jhl.mds.dao.repositories;

import com.jhl.mds.dao.entities.MySQLServer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MySQLServerRepository extends JpaRepository<MySQLServer, Integer> {
    List<MySQLServer> findByServerId(List<Integer> serverIds);

    MySQLServer findByServerId(Integer serverId);
}
