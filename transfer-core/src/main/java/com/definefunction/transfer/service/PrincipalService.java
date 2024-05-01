package com.definefunction.transfer.service;

import com.definefunction.transfer.exception.ObjectDoesNotExistsException;
import com.definefunction.transfer.exception.UserNotAuthorizedException;
import com.definefunction.transfer.model.DTO.PrincipalScopeDTO;
import com.definefunction.transfer.model.DTO.ScopeDTO;
import com.definefunction.transfer.model.Principal;
import com.definefunction.transfer.model.Scope;
import com.definefunction.transfer.model.ScopePrincipal;
import com.definefunction.transfer.model.pojo.Role;
import com.definefunction.transfer.repository.PrincipalRepository;
import com.definefunction.transfer.repository.ScopePrincipalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PrincipalService {

    @Autowired
    private PrincipalRepository principalRepository;

    @Autowired
    private ScopePrincipalRepository scopePrincipalRepository;

    @Autowired
    private ScopeService scopeService;

    public Optional<Principal> findPrincipalByUsername(String username){
        return principalRepository.findPrincipalByUsernameIs(username);
    }

    public Optional<Principal> findPrincipalByEmail(String email){
        return principalRepository.findPrincipalByEmailIs(email);
    }

    public void savePrincipal(Principal principal) {
        principalRepository.save(principal);
    }

    public Principal findPrincipalById(long principleId) {
        return principalRepository.getReferenceById(principleId);
    }

    public List<Principal> findPrincipalsByInScope(String id, Principal principal) {
        if (scopeService.existsById(Long.parseLong(id))) {
            Scope scope = scopeService.FindAllById(Long.parseLong(id));
            if (scopeService.whatIsTheRoleOfThePrincipalInTheScope(principal, scope) == Role.ADMIN) {
                return principalRepository.findPrincipalsInScope(scope.getId());
            } else {
                throw new UserNotAuthorizedException("User is not authorized to delete the scope.");
            }
        } else {
            throw new ObjectDoesNotExistsException("Scope does not exist for id "+id);
        }
    }

    public List<PrincipalScopeDTO> findScopedPrincipals(String id, Principal principal) {
        if (scopeService.existsById(Long.parseLong(id))) {
            Scope scope = scopeService.FindAllById(Long.parseLong(id));
            if (scopeService.whatIsTheRoleOfThePrincipalInTheScope(principal, scope) == Role.ADMIN) {
                List<PrincipalScopeDTO> scopePrincipals = new ArrayList<>();
                scopePrincipalRepository.findScopePrincipalsByScopeIs(scope).forEach(scopePrincipal -> {
                    long identification = scopePrincipal.getPrincipal().getId();
                    String name = scopePrincipal.getPrincipal().getUsername();
                    ScopeDTO scopeDTO = new ScopeDTO();
                    scopeDTO.setId(scopePrincipal.getScope().getId());
                    scopeDTO.setName(scopePrincipal.getScope().getName());
                    Role role = scopePrincipal.getRole();
                    scopePrincipals.add(new PrincipalScopeDTO(identification, name, scopeDTO,role));
                });
                return scopePrincipals;
            } else {
                throw new UserNotAuthorizedException("User is not authorized to delete the scope.");
            }
        } else {
            throw new ObjectDoesNotExistsException("Scope does not exist for id "+id);
        }
    }

    public List<Principal> findPrincipalsByNotInScope(String id, Principal principal) {
        if (scopeService.existsById(Long.parseLong(id))) {
            Scope scope = scopeService.FindAllById(Long.parseLong(id));
            if (scopeService.whatIsTheRoleOfThePrincipalInTheScope(principal, scope) == Role.ADMIN) {
                return principalRepository.findPrincipalsNotInScope(scope.getId());
            } else {
                throw new UserNotAuthorizedException("User is not authorized to delete the scope.");
            }
        } else {
            throw new ObjectDoesNotExistsException("Scope does not exist for id "+id);
        }
    }
}
