DROP TABLE IF EXISTS mysql_binlog_position;
CREATE TABLE mysql_binlog_position (
  mysql_binlog_position_id INTEGER PRIMARY KEY AUTO_INCREMENT,
  host                     VARCHAR(255),
  port                     VARCHAR(255),
  filename                 VARCHAR(255),
  position                 LONG,
  created_at               TIMESTAMP           DEFAULT CURRENT_TIMESTAMP,
  updated_at               TIMESTAMP           DEFAULT CURRENT_TIMESTAMP
  ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT host_port_unique UNIQUE (host, port)
);