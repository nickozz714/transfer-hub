package com.definefunction.transfer.service;

import com.definefunction.transfer.model.InProgressRecord;
import com.definefunction.transfer.repository.InProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InProgressService {

    @Autowired
    private InProgressRepository inProgressRepository;

    public boolean isInProgress(String transfer, String file) {
        return inProgressRepository.existsByTransferAndFile(transfer, file);
    }

    public List<InProgressRecord> findAll(){
        return inProgressRepository.findAll();
    }

    public List<InProgressRecord> findAllByTransfer(String transfer){
        return inProgressRepository.getAllByTransferIs(transfer);
    }

    public void insert(InProgressRecord inProgressRecord) {
        inProgressRepository.save(inProgressRecord);
    }

    public void delete(String transfer, String file) {
        List<InProgressRecord> inProgressRecordList = inProgressRepository.getAllByTransferIsAndFileIs(transfer, file);
        inProgressRepository.deleteAll(inProgressRecordList);
    }

    public void deleteAllByTransferRecord(String transfer) {
        List<InProgressRecord> inProgressRecordList = inProgressRepository.getAllByTransferIs(transfer);
        inProgressRepository.deleteAll(inProgressRecordList);
    }

}
