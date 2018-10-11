DROP TABLE IF EXISTS task_filter;
CREATE TABLE task_filter (
  filter_id  INTEGER PRIMARY KEY AUTOINCREMENT,
  fk_task_id INTEGER,
  filter     VARCHAR(255),
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  FOREIGN KEY (fk_task_id) REFERENCES task (task_id)
    ON DELETE CASCADE,
  CONSTRAINT task_filter UNIQUE (fk_task_id, filter)
);