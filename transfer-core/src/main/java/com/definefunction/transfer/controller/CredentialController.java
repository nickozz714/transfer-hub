package com.definefunction.transfer.controller;

import com.definefunction.transfer.exception.ObjectDoesNotExistsException;
import com.definefunction.transfer.model.Credential;
import com.definefunction.transfer.model.DTO.CredentialDTO;
import com.definefunction.transfer.model.Scope;
import com.definefunction.transfer.model.views.View;
import com.definefunction.transfer.service.CredentialService;
import com.definefunction.transfer.service.ScopeService;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping(CredentialController.URL)
public class CredentialController {

    public static final String URL = "api/credential";

    @Autowired
    private CredentialService credentialService;

    @Autowired
    private ScopeService scopeService;

    @GetMapping
    @JsonView(value = View.UserView.GET.class)
    public ResponseEntity<List<Credential>> getAllCredentials() {
        List<Credential> credentialList = credentialService.getAll();
        return new ResponseEntity<>(credentialList, HttpStatus.OK);
    }

    @GetMapping("/scope/{id}")
    public ResponseEntity<List<Credential>> findCredentialsByScope(@PathVariable long id) {
        if (scopeService.existsById(id)) {
            Scope scope = scopeService.FindAllById(id);
            return new ResponseEntity<>(credentialService.findByScope(scope), HttpStatus.OK);
        } else {
            throw new ObjectDoesNotExistsException("Scope does not exist.");
        }
    }

    @GetMapping("/{id}")
    @JsonView(value = View.UserView.GET.class)
    public ResponseEntity<Credential> getCredentialById(@PathVariable String id) {
        if(credentialService.exists(id)) {
            return new ResponseEntity<>(credentialService.findById(id), HttpStatus.OK);
        } else {
            throw new ObjectDoesNotExistsException("The credential does not exist");
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Credential> save(@RequestBody CredentialDTO credential, @RequestHeader(name = "Authorization") String token) throws Exception {
        return new ResponseEntity<>(credentialService.save(credential, token), HttpStatus.CREATED);
    }

    @PutMapping("/update")
    @JsonView(value=View.UserView.GET.class)
    public ResponseEntity<Credential> update(@RequestBody CredentialDTO credential, @RequestHeader(name = "Authorization") String token) throws Exception {
        if (credentialService.exists(String.valueOf(credential.getId()))){
            return new ResponseEntity<>(credentialService.save(credential, token),HttpStatus.OK);
        } else {
            throw new ObjectDoesNotExistsException("The credential does not exist");
        }
    }

    @DeleteMapping("/{credentialId}")
    public ResponseEntity<String> delete(@PathVariable String credentialId, @RequestHeader(name = "Authorization") String token) throws Exception {
        credentialService.delete(credentialId, token);
        return new ResponseEntity<>("Credential is removed",HttpStatus.NO_CONTENT);
    }
}
