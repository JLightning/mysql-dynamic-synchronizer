package com.jhl.mds.dao.repositories;

import com.jhl.mds.dao.entities.MySQLServer;
import com.jhl.mds.dao.entities.RedisServer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RedisServerRepository extends JpaRepository<RedisServer, Integer> {
    List<RedisServer> findByServerId(List<Integer> serverIds);

    RedisServer findByServerId(Integer serverId);
}
