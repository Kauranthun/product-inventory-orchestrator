package com.tutorial.crud.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;


@Service
public class DatabaseService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void exportDB(String path) {
        jdbcTemplate.execute("SCRIPT TO '" + path + "'");
    }

    public void importDB(String path) {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");

        try {
            jdbcTemplate.execute("DROP ALL OBJECTS");
            String normalizedPath = path.replace("\\", "/");
            jdbcTemplate.execute("RUNSCRIPT FROM '" + normalizedPath + "'");
            System.out.println("Restore Full DB: Success");
        } catch (Exception e) {
            System.err.println("Restore Failed: " + e.getMessage());
            throw new RuntimeException("Error during Full Restore", e);
        } finally {
            jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
        }
    }
}
