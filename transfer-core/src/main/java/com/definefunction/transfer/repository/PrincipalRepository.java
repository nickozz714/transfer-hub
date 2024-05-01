package com.definefunction.transfer.repository;

import com.definefunction.transfer.model.Principal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrincipalRepository extends JpaRepository<Principal, Long> {

    Optional<Principal> findPrincipalByUsernameIs(String username);

    Optional<Principal> findPrincipalByEmailIs(String email);

    @Query(value = "select * from principal p where p.id in (select principal_id from scope_principal where scope_id = :scope_id);", nativeQuery = true)
    List<Principal> findPrincipalsInScope(@Param("scope_id") long id);

    @Query(value = "select * from principal p where p.id not in (select principal_id from scope_principal where scope_id = :scope_id);", nativeQuery = true)
    List<Principal> findPrincipalsNotInScope(@Param("scope_id") long id);
}
