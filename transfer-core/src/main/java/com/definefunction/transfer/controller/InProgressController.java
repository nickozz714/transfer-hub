package com.definefunction.transfer.controller;

import com.definefunction.transfer.model.InProgressRecord;
import com.definefunction.transfer.service.InProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping(InProgressController.URL)
public class InProgressController {
    public static final String URL = "api/inprogress";

    @Autowired
    private InProgressService inProgressService;

    @GetMapping
    public ResponseEntity<List<InProgressRecord>> getAll(){
        return new ResponseEntity<>(inProgressService.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{route}")
    public ResponseEntity<List<InProgressRecord>> getAllByRoute(@PathVariable String route){
        return new ResponseEntity<>(inProgressService.findAllByTransfer(route), HttpStatus.OK);
    }

    @DeleteMapping("{route}/{file}")
    public ResponseEntity<InProgressRecord> deleteByTransferAndFile(@PathVariable String route, @PathVariable String file) {
        inProgressService.delete(route, file);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
