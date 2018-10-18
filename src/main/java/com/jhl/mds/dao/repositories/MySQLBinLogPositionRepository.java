package com.jhl.mds.dao.repositories;

import com.jhl.mds.dao.entities.MySQLBinLogPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface MySQLBinLogPositionRepository extends JpaRepository<MySQLBinLogPosition, Integer> {

    MySQLBinLogPosition findByHostAndPort(String host, String port);

    @Modifying
    @Query(value = "REPLACE INTO mysql_binlog_position (host, port, filename, position) VALUES (?1, ?2, ?3, ?4)", nativeQuery = true)
    @Transactional
    void updatePosition(String host, String port, String filename, long position);
}
