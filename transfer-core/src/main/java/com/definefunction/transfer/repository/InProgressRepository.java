package com.definefunction.transfer.repository;

import com.definefunction.transfer.model.InProgressRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InProgressRepository extends JpaRepository<InProgressRecord, String> {

    boolean existsByTransferAndFile(String transfer, String file);

    void deleteAllByTransferIsAndFileIs(String transfer, String file);

    List<InProgressRecord> getAllByTransferIsAndFileIs(String transfer, String file);

    List<InProgressRecord> getAllByTransferIs(String transfer);
}
