package com.jhl.mds.config;

import com.jhl.mds.dbmigration.DatabaseMigrationRunner;
import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;

@Configuration
@Import(DatabaseMigrationRunner.class)
public class Config {

    @Value("${spring.datasource.url:jdbc:sqlite:mds.db}")
    private String sqliteDatabaseFile;

    @Bean
    public LayoutDialect layoutDialect() {
        return new LayoutDialect();
    }

    @Bean
    public DataSource dataSource() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create()
                .driverClassName("org.sqlite.JDBC")
                .url(sqliteDatabaseFile);
        return dataSourceBuilder.build();
    }
}
