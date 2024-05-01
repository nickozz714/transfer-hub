package com.definefunction.transfer.controller;

import com.definefunction.transfer.model.ProgressEventsRecord;
import com.definefunction.transfer.service.ProgressEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping(ProgressEventsController.URL)
public class ProgressEventsController {

    @Autowired
    private ProgressEventService progressEventService;

    public static final String URL = "api/progressevent";

    @GetMapping
    public ResponseEntity<List<ProgressEventsRecord>> getAll(){
        return new ResponseEntity<>(progressEventService.getAll(), HttpStatus.OK);
    }

    @GetMapping("/{transfer}")
    public ResponseEntity<List<ProgressEventsRecord>> getAllByTransfer(@PathVariable String transfer) {
        return new ResponseEntity<>(progressEventService.getAllByTransfer(transfer), HttpStatus.OK);
    }
}
