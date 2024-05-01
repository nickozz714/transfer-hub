package com.definefunction.transfer.repository;

import com.definefunction.transfer.model.Principal;
import com.definefunction.transfer.model.Scope;
import com.definefunction.transfer.model.TransferRecord;
import com.definefunction.transfer.model.pojo.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferRecordRepository extends JpaRepository<TransferRecord, String> {

    List<TransferRecord> findAllByStatus(Status status);

    List<TransferRecord> findAllByScopeIs(Scope scope);

    @Query(value = "SELECT * FROM transfer_record tr where tr.scope_id in (SELECT sp.scope_id FROM scope_principal sp where sp.principal_id = :principle_id)", nativeQuery = true)
    List<TransferRecord> findAllByPrincipalInScope(@Param("principle_id") long principle_id);

    @Query(value="select * from transfer_record tr where tr.status = 0 and tr.id in (select transfer_record_id from endpoint e where e.credential_id = :id)", nativeQuery = true)
    List<TransferRecord> findAllTransferRecordsOnCredentialUpdateAndStatusEnabled(@Param("id") String id);

    @Query(value="select * from transfer_record tr where tr.id in (select transfer_record_id from endpoint e where e.credential_id = :id)", nativeQuery = true)
    List<TransferRecord> findAllTransferRecordsOnCredential(@Param("id") String id);

    @Query(value="select * from transfer_record tr where tr.status = 0 and tr.id in (select transfer_record_id from endpoint e where e.host_id = :id)", nativeQuery = true)
    List<TransferRecord> findAllTransferRecordsOnHostUpdateAndStatusEnabled(String id);

    @Query(value="select * from transfer_record tr where tr.id in (select transfer_record_id from endpoint e where e.host_id = :id)", nativeQuery = true)
    List<TransferRecord> findAllTransferRecordsOnHost(String id);
}
