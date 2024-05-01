package com.definefunction.transfer.repository;

import com.definefunction.transfer.model.Host;
import com.definefunction.transfer.model.Principal;
import com.definefunction.transfer.model.Scope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HostRepository extends JpaRepository<Host, Long> {

    List<Host> findHostsByScopeIs(Scope scope);

    @Query(value="SELECT * from host h where h.scope_id in (select sp.scope_id from scope_principal sp where sp.principal_id = :principle_id)", nativeQuery = true)
    List<Host> findHostsByPrincipalInScope(@Param("principle_id") long id);
}
