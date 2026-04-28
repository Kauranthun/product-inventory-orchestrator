package com.tutorial.crud.controller;

import com.tutorial.crud.dto.Message;
import com.tutorial.crud.service.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/db")
public class DatabaseController {
    @Autowired
    private DatabaseService databaseService;

    @PostMapping("/backup")
    public ResponseEntity<String> backup(){
        try{
            databaseService.exportDatabase("/main/ressources/backup.sql");
            return new ResponseEntity(new Message("Backup success !"), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(new Message("Backup error : "+e.getMessage()), HttpStatus.EXPECTATION_FAILED);
        }
    }

    @PostMapping("/restore")
    public ResponseEntity<String> restore(){
        try{
            databaseService.importDatabase("/main/ressources/backup.sql");
            return new ResponseEntity(new Message("Restore success !"), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(new Message("Restore error : "+e.getMessage()), HttpStatus.EXPECTATION_FAILED);
        }
    }
}
