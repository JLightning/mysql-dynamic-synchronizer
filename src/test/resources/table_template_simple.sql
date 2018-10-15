CREATE TABLE {table_name} (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `random_number` int(11) NOT NULL,
  `random_text` varchar(255) DEFAULT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8