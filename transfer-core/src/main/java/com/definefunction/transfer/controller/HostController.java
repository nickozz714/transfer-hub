package com.definefunction.transfer.controller;

import com.definefunction.transfer.exception.ObjectDoesNotExistsException;
import com.definefunction.transfer.exception.UserNotAuthorizedException;
import com.definefunction.transfer.model.DTO.HostDTO;
import com.definefunction.transfer.model.Host;
import com.definefunction.transfer.model.Principal;
import com.definefunction.transfer.model.Scope;
import com.definefunction.transfer.model.TransferRecord;
import com.definefunction.transfer.repository.HostRepository;
import com.definefunction.transfer.repository.ScopeRepository;
import com.definefunction.transfer.service.*;
import jakarta.persistence.PersistenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping(HostController.URL)
public class HostController {

    public static final String URL = "api/host";

    @Autowired
    private HostService hostService;

    @Autowired
    private ScopeService scopeService;

    @GetMapping
    public ResponseEntity<List<Host>> findAllHosts(@RequestHeader(name = "Authorization") String token){
        return new ResponseEntity<>(hostService.findAllHosts(token), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Host> findHostById(@PathVariable long id) {
        return new ResponseEntity<>(hostService.findHostById(id), HttpStatus.OK);
    }

    @GetMapping("/scope/{id}")
    public ResponseEntity<List<Host>> findHostByScope(@PathVariable long id) {
        if (scopeService.existsById(id)) {
            Scope scope = scopeService.FindAllById(id);
            return new ResponseEntity<>(hostService.findHostsByScopeIs(scope),HttpStatus.OK);
        } else {
            throw new ObjectDoesNotExistsException("Host with id: "+id+" does not exist.");
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Host> createNewHost(@RequestBody HostDTO host, @RequestHeader(name = "Authorization") String token) {
        return new ResponseEntity<>(hostService.save(token, host), HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<Host> updateExistingHost(@RequestBody HostDTO host, @RequestHeader(name = "Authorization") String token) {
        return new ResponseEntity<>(hostService.update(token, host), HttpStatus.OK);
    }

    @DeleteMapping("/remove/{id}")
    public ResponseEntity<String> removeExistingHost(@PathVariable String id, @RequestHeader(name = "Authorization") String token) {
        if (hostService.remove(token, Long.valueOf(id))) {
            return new ResponseEntity<>("Host has been removed succesfully", HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
