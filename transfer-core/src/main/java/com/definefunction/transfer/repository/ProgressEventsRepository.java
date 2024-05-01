package com.definefunction.transfer.repository;

import com.definefunction.transfer.model.ProgressEventsRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgressEventsRepository extends JpaRepository<ProgressEventsRecord, String> {

    List<ProgressEventsRecord> findAllByTransferIs(String transfer);

}
