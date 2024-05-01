package com.definefunction.transfer.controller;

import com.definefunction.transfer.exception.ObjectAlreadyExistsException;
import com.definefunction.transfer.exception.ObjectDoesNotExistsException;
import com.definefunction.transfer.model.DTO.PrincipalScopeDTO;
import com.definefunction.transfer.model.DTO.ScopeDTO;
import com.definefunction.transfer.model.Principal;
import com.definefunction.transfer.model.Scope;
import com.definefunction.transfer.model.ScopePrincipal;
import com.definefunction.transfer.service.AuthenticationService;
import com.definefunction.transfer.service.PrincipalService;
import com.definefunction.transfer.service.ScopeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin("*")
@RestController
@RequestMapping("/api/scope")
public class ScopeController {

    @Autowired
    private ScopeService scopeService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private PrincipalService principalService;

    @GetMapping("/all")
    public ResponseEntity<List<Scope>> getAllScopes(){
        return new ResponseEntity<>(scopeService.FindAllScopes(), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Scope>> getAllAuthorizedScopes(@RequestHeader(name = "Authorization") String token) {
        Principal principal = authenticationService.retrievePrincipalFromToken(token);
        List<Scope> scopes = scopeService.FindAllScopesForPrincipal(principal);
        return new ResponseEntity<>(scopes, HttpStatus.OK);
    }

    @GetMapping("/principal/scoped/{id}")
    public ResponseEntity<List<PrincipalScopeDTO>> getAllPrincipalScopes(@PathVariable String id, @RequestHeader(name = "Authorization") String token) {
        Principal principal = authenticationService.retrievePrincipalFromToken(token);
        List<PrincipalScopeDTO> scopedPrincipals = principalService.findScopedPrincipals(id, principal);
        return new ResponseEntity<>(scopedPrincipals, HttpStatus.OK);
    }

    @GetMapping("/principal/in/{id}")
    public ResponseEntity<List<Principal>> getAllPrincipalsInScope(@PathVariable String id, @RequestHeader(name = "Authorization") String token) {
        Principal principal = authenticationService.retrievePrincipalFromToken(token);
        List<Principal> principals = principalService.findPrincipalsByInScope(id, principal);
        return new ResponseEntity<>(principals, HttpStatus.OK);
    }

    @GetMapping("/principal/out/{id}")
    public ResponseEntity<List<Principal>> getAllPrincipalsNotInScope(@PathVariable String id, @RequestHeader(name = "Authorization") String token) {
        Principal principal = authenticationService.retrievePrincipalFromToken(token);
        List<Principal> principals = principalService.findPrincipalsByNotInScope(id, principal);
        return new ResponseEntity<>(principals, HttpStatus.OK);
    }

    @GetMapping("/admin")
    public ResponseEntity<List<Scope>> getAllAuthorizedAdminScopes(@RequestHeader(name = "Authorization") String token) {
        Principal principal = authenticationService.retrievePrincipalFromToken(token);
        List<Scope> scopes = scopeService.FindAllScopesForPrincipalWithRoleAdmin(principal);
        return new ResponseEntity<>(scopes, HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<Scope> createNewScope(@RequestBody ScopeDTO scopeDTO, @RequestHeader(name = "Authorization") String token) throws ObjectAlreadyExistsException {
        Principal principal = authenticationService.retrievePrincipalFromToken(token);
        if (!scopeService.existsByName(scopeDTO.getName())) {
            Scope scope = new Scope();
            scope.setName(scopeDTO.getName());
            scopeService.CreateScope(scope,principal);
            return new ResponseEntity<>(scope, HttpStatus.CREATED);
        } else {
            throw new ObjectAlreadyExistsException("Scope already exists");
        }
    }

    @PutMapping("/update")
    public ResponseEntity<Scope> updateExistingScope(@RequestBody ScopeDTO scopeDTO, @RequestHeader(name = "Authorization") String token) throws ObjectDoesNotExistsException {
        Principal principal = authenticationService.retrievePrincipalFromToken(token);
        if (scopeService.existsById(scopeDTO.getId())) {
            Scope scope = scopeService.FindAllById(scopeDTO.getId());
            scope.setName(scopeDTO.getName());
            scopeService.UpdateScope(scope, principal);
            return new ResponseEntity<>(scope, HttpStatus.OK);
        } else {
            throw new ObjectDoesNotExistsException("Scope doesn't exist");
        }
    }

    @PutMapping("/join")
    public ResponseEntity<String> joinExistingScope(@RequestBody PrincipalScopeDTO principalScopeDTO, @RequestHeader(name = "Authorization") String token) {
        Principal principal = authenticationService.retrievePrincipalFromToken(token);
        scopeService.AddOrUpdatePrincipalToScope(
                scopeService.FindAllById(principalScopeDTO.getScopeDTO().getId()),
                principalService.findPrincipalById(principalScopeDTO.getPrinciple_id()),
                principalScopeDTO.getRole());
    return new ResponseEntity<>("Added/updated scope principal", HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Scope> deleteExistingScope(@RequestBody ScopeDTO scopeDTO, @RequestHeader(name="Authorization") String token) throws ObjectDoesNotExistsException {
        Principal principal = authenticationService.retrievePrincipalFromToken(token);
        if (scopeService.existsById(scopeDTO.getId())) {
            Scope scope = scopeService.FindAllById(scopeDTO.getId());
            scopeService.DeleteScope(scope,principal);
            return new ResponseEntity<>(scope, HttpStatus.NO_CONTENT);
        } else {
            throw new ObjectDoesNotExistsException("Scope doesn't exist exists");
        }
    }
}
