package com.definefunction.transfer.repository;

import com.definefunction.transfer.model.ParameterArchived;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParameterArchivedRepository extends JpaRepository<ParameterArchived, String> {
}
