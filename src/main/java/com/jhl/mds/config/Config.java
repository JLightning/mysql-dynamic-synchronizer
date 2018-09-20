package com.jhl.mds.config;

import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
public class Config implements WebMvcConfigurer {

    @Value("${spring.datasource.url:jdbc:sqlite:mds.db}")
    private String sqliteDatabaseFile;

    @Bean
    public LayoutDialect layoutDialect() {
        return new LayoutDialect();
    }

    @Bean
    public DataSource dataSource() throws SQLException {
        DataSource dataSource = DataSourceBuilder.create()
                .driverClassName("org.sqlite.JDBC")
                .url(sqliteDatabaseFile)
                .build();

        return dataSource;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/**").addResourceLocations("classpath:/templates/index.html");
        registry.addResourceHandler("/public/**").addResourceLocations("classpath:/static/resources/");
    }
}
