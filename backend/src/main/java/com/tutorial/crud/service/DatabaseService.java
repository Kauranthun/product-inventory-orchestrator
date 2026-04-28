package com.tutorial.crud.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DatabaseService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void exportDatabase(String path) {
        jdbcTemplate.execute("SCRIPT TO '" + path + "'");
    }

    public void importDatabase(String path) {
        jdbcTemplate.execute("DROP ALL OBJECTS");
        jdbcTemplate.execute("RUNSCRIPT FROM '" + path + "'");
    }
}
