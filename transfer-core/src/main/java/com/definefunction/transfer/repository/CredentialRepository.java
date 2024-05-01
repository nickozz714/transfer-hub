package com.definefunction.transfer.repository;

import com.definefunction.transfer.model.Credential;
import com.definefunction.transfer.model.Scope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CredentialRepository extends JpaRepository<Credential, String> {

    List<Credential> findCredentialsByScopeIs(Scope scope);

    @Query(value="select * from credential c where c.id = :id and c.id in (SELECT credential_id from transfer_record tr join endpoint e on tr.id  = e.transfer_record_id where tr.status = 0);", nativeQuery = true)
    List<Credential> findCredentialInActiveRoutes(@Param("id") String id);
}
