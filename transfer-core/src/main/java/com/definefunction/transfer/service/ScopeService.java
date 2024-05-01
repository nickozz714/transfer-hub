package com.definefunction.transfer.service;

import com.definefunction.transfer.exception.UserNotAuthorizedException;
import com.definefunction.transfer.model.Principal;
import com.definefunction.transfer.model.Scope;
import com.definefunction.transfer.model.ScopePrincipal;
import com.definefunction.transfer.model.pojo.Role;
import com.definefunction.transfer.repository.ScopePrincipalRepository;
import com.definefunction.transfer.repository.ScopeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
public class ScopeService {

    @Autowired
    private ScopeRepository scopeRepository;

    @Autowired
    private ScopePrincipalRepository scopePrincipalRepository;

    public List<Scope> FindAllScopes() {
        return scopeRepository.findAll();
    }

    public Scope FindAllById(long id) {
        return scopeRepository.findById(id).orElseThrow();
    }

    public List<Scope> FindAllScopesForPrincipal(Principal principal) {
        return scopeRepository.findScopesByPrincipals(principal.getId());
    }

    public Role whatIsTheRoleOfThePrincipalInTheScope(Principal principal, Scope scope) {
        ScopePrincipal scopePrincipal = scopePrincipalRepository.findScopePrincipalByScopeIsAndPrincipalIs(scope, principal);
        if (scopePrincipal != null) {
            return scopePrincipal.getRole();
        } else {
            return null;
        }
    }

    public boolean existsById(long id) {
        return scopeRepository.existsById(id);
    }

    public boolean existsByName(String name) {
        return scopeRepository.findScopeByNameIs(name).isPresent();
    }

    public void CreateScope(Scope scope, Principal principal) {
        // Every user can create a new Scope and will automatically become it's admin.
        scopeRepository.save(scope);
        AddOrUpdatePrincipalToScope(scope, principal, Role.ADMIN);
    }

    public void UpdateScope(Scope scope, Principal principal) throws UserNotAuthorizedException {
        // Updating scopes requires the user to be an admin.
        if (whatIsTheRoleOfThePrincipalInTheScope(principal, scope) == Role.ADMIN) {
            scopeRepository.save(scope);
        } else {
            throw new UserNotAuthorizedException("User is not authorized to update the scope.");
        }
    }

    public void DeleteScope(Scope scope, Principal principal) throws UserNotAuthorizedException {
        if (whatIsTheRoleOfThePrincipalInTheScope(principal, scope) == Role.ADMIN) {
            scopeRepository.delete(scope);
        } else {
            throw new UserNotAuthorizedException("User is not authorized to delete the scope.");
        }
    }

    public void AddOrUpdatePrincipalToScope(Scope scope, Principal principal, Role role) {
        ScopePrincipal scopePrincipal = scopePrincipalRepository.findScopePrincipalByScopeIsAndPrincipalIs(scope, principal);
        if (scopePrincipal == null) {
            ScopePrincipal newScopePrinciple = new ScopePrincipal();
            newScopePrinciple.setPrincipal(principal);
            newScopePrinciple.setScope(scope);
            newScopePrinciple.setRole(role);
            newScopePrinciple.setLast_changed_on(new Date());
            scopePrincipalRepository.save(newScopePrinciple);
        } else {
            scopePrincipal.setPrincipal(principal);
            scopePrincipal.setRole(role);
            scopePrincipal.setLast_changed_on(new Date());
            scopePrincipalRepository.save(scopePrincipal);
        }
    }

    public List<Scope> FindAllScopesForPrincipalWithRoleAdmin(Principal principal) {
        return scopeRepository.findScopesByPrincipalsWithRoleAdmin(principal.getId());
    }
}
