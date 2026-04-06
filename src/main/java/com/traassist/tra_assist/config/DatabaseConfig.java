package com.traassist.tra_assist.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DatabaseConfig {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void enablePgVector() {
        jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
    }
}