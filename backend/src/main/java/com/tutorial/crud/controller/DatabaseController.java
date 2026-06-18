package com.tutorial.crud.controller;

import com.tutorial.crud.dto.Message;
import com.tutorial.crud.service.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Role;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController

@RequestMapping("/db")
public class DatabaseController {
    @Autowired
    private DatabaseService databaseService;

    private static final List<String> ENTITY_TABLES = List.of("product");

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @PostMapping("/backup")
    public ResponseEntity<?> backup() {
        try {
            databaseService.exportDB("./backup.sql");
            return ResponseEntity.ok(new Message("Backup successful !"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Message("Error Backup : " + e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @PostMapping("/restore")
    public ResponseEntity<?> restore() {
        try {
            databaseService.importDB("./backup.sql");
            return ResponseEntity.ok(new Message("Restore successful !"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                    .body(new Message("Error Restore : " + e.getMessage()));
        }
    }
}
