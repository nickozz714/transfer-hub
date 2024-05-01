package com.definefunction.transfer.repository;

import com.definefunction.transfer.model.EndpointHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EndpointHistoryRepository extends JpaRepository<EndpointHistory, String> {
}
