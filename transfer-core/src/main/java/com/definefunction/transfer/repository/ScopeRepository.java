package com.definefunction.transfer.repository;

import com.definefunction.transfer.model.Principal;
import com.definefunction.transfer.model.Scope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScopeRepository extends JpaRepository<Scope, Long> {

    @Query(value = "SELECT * from scope s where s.id in (SELECT sp.scope_id from scope_principal sp where sp.principal_id = :principal_id)", nativeQuery = true)
    List<Scope> findScopesByPrincipals(@Param("principal_id") long id);

    Optional<Scope> findScopeByNameIs(String name);

    @Query(value = "SELECT * from scope s where s.id in (SELECT sp.scope_id from scope_principal sp where sp.principal_id = :principal_id and role = 0)", nativeQuery = true)
    List<Scope> findScopesByPrincipalsWithRoleAdmin(@Param("principal_id") long id);
}
