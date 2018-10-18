DROP TABLE IF EXISTS mysql_server;
CREATE TABLE mysql_server (
  server_id  INTEGER PRIMARY KEY AUTO_INCREMENT,
  name       VARCHAR(1024) UNIQUE,
  host       VARCHAR(255),
  port       VARCHAR(255),
  username   VARCHAR(127),
  password   VARCHAR(127),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

DROP TABLE IF EXISTS task;
CREATE TABLE task (
  task_id                      INTEGER PRIMARY KEY AUTO_INCREMENT,
  name                         VARCHAR(1024) UNIQUE,
  fk_source_server             INTEGER,
  source_database              VARCHAR(255),
  source_table                 VARCHAR(255),
  fk_target_server             INTEGER,
  target_database              VARCHAR(255),
  target_table                 VARCHAR(255),
  migration_type               INTEGER,
  task_type                    INTEGER,
  insert_mode                  VARCHAR(80),
  full_migration_progress      DOUBLE,
  incremental_migration_active INTEGER,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (fk_source_server) REFERENCES mysql_server (server_id)
    ON DELETE CASCADE,
  FOREIGN KEY (fk_target_server) REFERENCES mysql_server (server_id)
    ON DELETE CASCADE
);

DROP TABLE IF EXISTS task_field_mapping;
CREATE TABLE task_field_mapping (
  mapping_id   INTEGER PRIMARY KEY AUTO_INCREMENT,
  fk_task_id   INTEGER,
  source_field VARCHAR(255),
  target_field VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (fk_task_id) REFERENCES task (task_id)
    ON DELETE CASCADE,
  CONSTRAINT task_source_target_field UNIQUE (fk_task_id, source_field, target_field)
);