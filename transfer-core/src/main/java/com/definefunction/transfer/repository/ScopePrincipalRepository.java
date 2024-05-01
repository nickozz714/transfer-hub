package com.definefunction.transfer.repository;

import com.definefunction.transfer.model.Principal;
import com.definefunction.transfer.model.Scope;
import com.definefunction.transfer.model.ScopePrincipal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScopePrincipalRepository extends JpaRepository<ScopePrincipal, Long> {

    ScopePrincipal findScopePrincipalByScopeIsAndPrincipalIs(Scope scope, Principal principal);

    List<ScopePrincipal> findScopePrincipalsByScopeIs(Scope scope);
}
