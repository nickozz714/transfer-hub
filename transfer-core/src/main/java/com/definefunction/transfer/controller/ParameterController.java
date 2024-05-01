package com.definefunction.transfer.controller;

import com.definefunction.transfer.exception.ObjectDoesNotExistsException;
import com.definefunction.transfer.model.InProgressRecord;
import com.definefunction.transfer.model.Parameter;
import com.definefunction.transfer.service.ParameterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api/parameter")
public class ParameterController {

    @Autowired
    private ParameterService parameterService;

    @GetMapping
    public ResponseEntity<List<Parameter>> getAll(){
        return new ResponseEntity<>(parameterService.getAllParameters(), HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<List<Parameter>> save(@RequestBody Parameter parameter){
        return new ResponseEntity<>(parameterService.save(parameter), HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<List<Parameter>> update(@RequestBody Parameter parameter) {
        if (parameterService.exists(parameter)) {
            return new ResponseEntity<>(parameterService.save(parameter), HttpStatus.OK);
        } else {
            throw new ObjectDoesNotExistsException("Unknown Parameter");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable String id) {
        if (parameterService.existsById(id)) {
            parameterService.delete(parameterService.findParameterById(id));
            return new ResponseEntity<>("Parameter is removed.", HttpStatus.OK);
        } else {
            throw new ObjectDoesNotExistsException("Unknown Parameter");
        }
    }

}
