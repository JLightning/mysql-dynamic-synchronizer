package com.jhl.mds;

import com.jhl.mds.dbmigration.DatabaseMigrationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@ComponentScan(basePackages = "com.jhl.mds", excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASPECTJ, pattern = "com.jhl.mds.dbmigration.*"),
        @ComponentScan.Filter(type = FilterType.ASPECTJ, pattern = "com.jhl.mds.jsclientgenerator.*.*")
})
public class Application {

    public static void main(String[] args) {
        DatabaseMigrationRunner.start();
        ApplicationContext context = SpringApplication.run(Application.class);
    }
}
