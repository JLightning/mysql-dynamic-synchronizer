DROP TABLE IF EXISTS task_statistics;
CREATE TABLE task_statistics (
  task_statistics_id INTEGER PRIMARY KEY AUTO_INCREMENT,
  fk_task_id         INTEGER,
  insert_count       INTEGER,
  update_count       INTEGER,
  delete_count       INTEGER,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (fk_task_id) REFERENCES task (task_id)
    ON DELETE CASCADE,
  CONSTRAINT task UNIQUE (fk_task_id)
);