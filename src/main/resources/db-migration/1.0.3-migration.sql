DROP TABLE IF EXISTS redis_server;
CREATE TABLE redis_server (
  server_id  INTEGER PRIMARY KEY AUTO_INCREMENT,
  name       VARCHAR(1024) UNIQUE,
  host       VARCHAR(255),
  port       VARCHAR(255),
  username   VARCHAR(127),
  password   VARCHAR(127),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX redis_server_hpup
  ON redis_server (host, port, username, password);